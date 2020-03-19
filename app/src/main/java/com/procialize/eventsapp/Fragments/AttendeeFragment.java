package com.procialize.eventsapp.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.procialize.eventsapp.Activity.AttendeeDetailActivity;
import com.procialize.eventsapp.Activity.LoginActivity;
import com.procialize.eventsapp.Adapter.AttendeeAdapter;
import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.DbHelper.ConnectionDetector;
import com.procialize.eventsapp.DbHelper.DBHelper;
import com.procialize.eventsapp.GetterSetter.AttendeeList;
import com.procialize.eventsapp.GetterSetter.EventSettingList;
import com.procialize.eventsapp.GetterSetter.FetchAttendee;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import cn.jzvd.JZVideoPlayer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction eventsapp.
 * Use the {@link AttendeeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendeeFragment extends Fragment implements AttendeeAdapter.AttendeeAdapterListner {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    RecyclerView attendeerecycler;
    SwipeRefreshLayout attendeefeedrefresh;
    EditText searchEt;
    AttendeeAdapter attendeeAdapter;
    List<EventSettingList> eventSettingLists;
    SessionManager sessionManager;
    String eventid,colorActive;
    TextView pullrefresh;
    String MY_PREFS_NAME = "ProcializeInfo";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private APIService mAPIService;
    private ProgressBar progressBar;
    private DBHelper procializeDB;
    private SQLiteDatabase db;
    private ConnectionDetector cd;
    private List<AttendeeList> attendeeList;
    private List<AttendeeList> attendeesDBList;
    private DBHelper dbHelper;
    LinearLayout linear;


    public AttendeeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AttendeeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AttendeeFragment newInstance(String param1, String param2) {
        AttendeeFragment fragment = new AttendeeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_attendee, container, false);


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        attendeerecycler = view.findViewById(R.id.attendeerecycler);
        searchEt = view.findViewById(R.id.searchEt);
        attendeefeedrefresh = view.findViewById(R.id.attendeefeedrefresh);
        progressBar = view.findViewById(R.id.progressBar);
        linear = view.findViewById(R.id.linear);
        pullrefresh = view.findViewById(R.id.pullrefresh);
        cd = new ConnectionDetector(getActivity());
        dbHelper = new DBHelper(getActivity());
        sessionManager = new SessionManager(getContext());

        procializeDB = new DBHelper(getActivity());
        db = procializeDB.getWritableDatabase();

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        attendeerecycler.setLayoutManager(mLayoutManager);

        int resId = R.anim.layout_animation_slide_right;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        // attendeerecycler.setLayoutAnimation(animation);

        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");
        colorActive = prefs.getString("colorActive", "");

        pullrefresh.setTextColor(Color.parseColor(colorActive));

        mAPIService = ApiUtils.getAPIService();

        SessionManager sessionManager = new SessionManager(getContext());

        HashMap<String, String> user = sessionManager.getUserDetails();
        try {

            File mypath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Procialize/" + "background.jpg");
            Resources res = getResources();
            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(mypath));
            BitmapDrawable bd = new BitmapDrawable(res, bitmap);
            linear.setBackgroundDrawable(bd);

            Log.e("PATH", String.valueOf(mypath));
        } catch (Exception e) {
            e.printStackTrace();
            linear.setBackgroundColor(Color.parseColor("#f1f1f1"));

        }

        // token
        final String token = user.get(SessionManager.KEY_TOKEN);

        if (cd.isConnectingToInternet()) {
            fetchFeed(token, eventid);
        } else {
            db = procializeDB.getReadableDatabase();

            attendeesDBList = dbHelper.getAttendeeDetails();

            attendeeAdapter = new AttendeeAdapter(getActivity(), attendeesDBList, this);
            attendeeAdapter.notifyDataSetChanged();
            attendeerecycler.setAdapter(attendeeAdapter);
            attendeerecycler.scheduleLayoutAnimation();
        }

        attendeefeedrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (cd.isConnectingToInternet()) {
                    fetchFeed(token, eventid);
                } else {
                    attendeesDBList = dbHelper.getAttendeeDetails();

                    attendeeAdapter = new AttendeeAdapter(getActivity(), attendeesDBList, AttendeeFragment.this);
                    attendeeAdapter.notifyDataSetChanged();
                    attendeerecycler.setAdapter(attendeeAdapter);
                    attendeerecycler.scheduleLayoutAnimation();
                }
            }
        });


        try {
            searchEt.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                    if (attendeeAdapter != null) {
                        try {
                            attendeeAdapter.getFilter().filter(s.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }


    public void fetchFeed(String token, String eventid) {
        progressBar.setVisibility(View.VISIBLE);
        mAPIService.AttendeeFetchPost(token, eventid).enqueue(new Callback<FetchAttendee>() {
            @Override
            public void onResponse(Call<FetchAttendee> call, Response<FetchAttendee> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());
                    progressBar.setVisibility(View.GONE);
                    if (attendeefeedrefresh.isRefreshing()) {
                        attendeefeedrefresh.setRefreshing(false);
                    }
                    if (response.body().getMsg().equalsIgnoreCase("Invalid Token!")) {
                        sessionManager.logoutUser();
                        Intent main = new Intent(getContext(), LoginActivity.class);
                        startActivity(main);
                        getActivity().finish();
                    } else {
                        showResponse(response);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);

                    if (attendeefeedrefresh.isRefreshing()) {
                        attendeefeedrefresh.setRefreshing(false);
                    }
                    //Toast.makeText(getContext(), "Unable to process", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FetchAttendee> call, Throwable t) {
                Log.e("hit", "Unable to submit post to API.");
                //  Toast.makeText(getContext(), "Unable to process", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);

                if (attendeefeedrefresh.isRefreshing()) {
                    attendeefeedrefresh.setRefreshing(false);
                }
            }
        });
    }

    public void showResponse(Response<FetchAttendee> response) {

        // specify an adapter (see also next example)
        attendeeList = response.body().getAttendeeList();
        procializeDB.clearAttendeesTable();
        procializeDB.insertAttendeesInfo(attendeeList, db);
        //attendeesDBList = dbHelper.getAttendeeDetails();


        attendeeAdapter = new AttendeeAdapter(getActivity(), response.body().getAttendeeList(), this);
        attendeeAdapter.notifyDataSetChanged();
        attendeerecycler.setAdapter(attendeeAdapter);
        attendeerecycler.scheduleLayoutAnimation();

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onContactSelected(AttendeeList attendee) {
        Intent attendeetail = new Intent(getContext(), AttendeeDetailActivity.class);
        attendeetail.putExtra("id", attendee.getAttendeeId());
        attendeetail.putExtra("name", attendee.getFirstName() + " " + attendee.getLastName());
        attendeetail.putExtra("city", attendee.getCity());
        attendeetail.putExtra("country", attendee.getCountry());
        attendeetail.putExtra("company", attendee.getCompanyName());
        attendeetail.putExtra("designation", attendee.getDesignation());
        attendeetail.putExtra("description", attendee.getDescription());
        attendeetail.putExtra("profile", attendee.getProfilePic());
        attendeetail.putExtra("mobile", attendee.getMobile());
//                speakeretail.putExtra("totalrate",attendee.getTotalRating());
        startActivity(attendeetail);
//        getActivity().finish();
    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    @Override
    public void onPause() {
        super.onPause();

        JZVideoPlayer.releaseAllVideos();

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
