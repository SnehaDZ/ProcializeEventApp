package com.procialize.eventsapp.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.procialize.eventsapp.Activity.ExhibitorDetailActivity;
import com.procialize.eventsapp.Activity.ExhibitorListingActivity;
import com.procialize.eventsapp.Activity.HomeActivity;
import com.procialize.eventsapp.Activity.LoginActivity;
import com.procialize.eventsapp.Adapter.ExhibitorAdapter;
import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.BuildConfig;
import com.procialize.eventsapp.DbHelper.ConnectionDetector;
import com.procialize.eventsapp.DbHelper.DBHelper;
import com.procialize.eventsapp.GetterSetter.ExhibitorDataList;
import com.procialize.eventsapp.GetterSetter.ExhibitorList;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AllExhibitorFragment extends Fragment implements ExhibitorAdapter.ExhibitorAdapterListner {

    RecyclerView exhibitorrecycler;
    SwipeRefreshLayout eventrefresh;
    ExhibitorAdapter eventAdapter;
    String MY_PREFS_NAME = "ProcializeInfo";
    EditText searchEt;
    ImageView img_logout, imageview;
    SessionManager session;
    String platform, device, os_version, app_version, accesstoken, eventid, colorActive;
    private APIService mAPIService;
    private ProgressBar progressBar;
    LinearLayout linear;
    ConnectionDetector cd;
    String url, CatName, CatId, eventnamestr;
    HashMap<String, String> user;
    private List<ExhibitorDataList> ExhibitorDataList = new ArrayList<>();
    SharedPreferences prefs;
    private DBHelper procializeDB;
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    TextView pullrefresh;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_exhibitor, container, false);
        exhibitorrecycler = view.findViewById(R.id.exhibitorrecycler);
        eventrefresh = view.findViewById(R.id.eventrefresh);
        prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, getActivity().MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");
        eventnamestr = prefs.getString("eventnamestr", "");
        colorActive = prefs.getString("colorActive", "");
        session = new SessionManager(getActivity());
        user = session.getUserDetails();

        // token
        accesstoken = user.get(SessionManager.KEY_TOKEN);
        cd = new ConnectionDetector(getActivity());
        CatId = "0";
        CatName = "All Category";
        platform = "android";
        device = Build.MODEL;
        os_version = Build.VERSION.RELEASE;
        app_version = "Version" + BuildConfig.VERSION_NAME;
        eventrefresh = view.findViewById(R.id.eventrefresh);
        progressBar = view.findViewById(R.id.progressBar);
        searchEt = view.findViewById(R.id.searchEt);
        img_logout = view.findViewById(R.id.img_logout);
        linear = view.findViewById(R.id.linear);
        imageview = view.findViewById(R.id.imageview);
        pullrefresh = view.findViewById(R.id.pullrefresh);
        dbHelper = new DBHelper(getActivity());

        pullrefresh.setTextColor(Color.parseColor(colorActive));
        procializeDB = new DBHelper(getActivity());
        db = procializeDB.getWritableDatabase();

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        exhibitorrecycler.setLayoutManager(mLayoutManager);


        mAPIService = ApiUtils.getAPIService();


        sendExhiList(eventid, accesstoken);

        eventrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ExhibitorDataList.size() > 0)
                    ExhibitorDataList.clear();

                sendExhiList(eventid, accesstoken);
            }
        });


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
                try {
                    eventAdapter.getFilter().filter(s.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        return view;
    }

    public void sendExhiList(String event_id, String token) {
        mAPIService.ExhibitorFetch(event_id, token).enqueue(new Callback<ExhibitorList>() {
            @Override
            public void onResponse(Call<ExhibitorList> call, Response<ExhibitorList> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());
                    dismissProgress();
                    if (response.body().getMsg().equalsIgnoreCase("Invalid Token!")) {
                        Intent main = new Intent(getActivity(), LoginActivity.class);
                        startActivity(main);

                    } else {
                        showResponse(response);
                    }
                } else {
                    dismissProgress();
                    Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
                    Log.i("hit", "post submitted to API Wrong.");
                }
            }

            @Override
            public void onFailure(Call<ExhibitorList> call, Throwable t) {
                Toast.makeText(getActivity(), "Low network or no network", Toast.LENGTH_SHORT).show();
                dismissProgress();
            }
        });
    }

    public void showResponse(Response<ExhibitorList> response) {

        if (response.body().getStatus().equals("success")) {
            dbHelper.clearCatagoryTable();
            dbHelper.clearEXCatagoryTable();
            dbHelper.insertExebitorTable(response.body().getExhibitorCatList(), db);
            dbHelper.insertExebitorCatTable(response.body().getExhiCatDetailList(), db);


            if (CatId.equalsIgnoreCase("0")) {
                eventAdapter = new ExhibitorAdapter(getActivity(), response.body().getExhibitorDataList(), this, CatName);
                eventAdapter.notifyDataSetChanged();
                exhibitorrecycler.setAdapter(eventAdapter);
                exhibitorrecycler.scheduleLayoutAnimation();

            } else {
                for (int i = 0; i < response.body().getExhibitorDataList().size(); i++) {
                    if (response.body().getExhibitorDataList().get(i).getExhibitor_category_id().equalsIgnoreCase(CatId)) {
                        ExhibitorDataList tempExhi = new ExhibitorDataList();
                        tempExhi.setName(response.body().getExhibitorDataList().get(i).getName());
                        tempExhi.setExhibitor_category_id(response.body().getExhibitorDataList().get(i).getExhibitor_category_id());
                        tempExhi.setAddress(response.body().getExhibitorDataList().get(i).getAddress());

                        tempExhi.setAllowed_message(response.body().getExhibitorDataList().get(i).getAllowed_message());
                        tempExhi.setAllowed_setup_meeting(response.body().getExhibitorDataList().get(i).getAllowed_setup_meeting());
                        tempExhi.setCreated(response.body().getExhibitorDataList().get(i).getCreated());
                        tempExhi.setEvent_id(response.body().getExhibitorDataList().get(i).getEvent_id());
                        tempExhi.setExhibitor_id(response.body().getExhibitorDataList().get(i).getExhibitor_id());
                        tempExhi.setLogo(response.body().getExhibitorDataList().get(i).getLogo());
                        tempExhi.setModified(response.body().getExhibitorDataList().get(i).getModified());
                        tempExhi.setStall_number(response.body().getExhibitorDataList().get(i).getStall_number());
                        tempExhi.setTile_image(response.body().getExhibitorDataList().get(i).getTile_image());

                        ExhibitorDataList.add(tempExhi);
                    }
                }

                eventAdapter = new ExhibitorAdapter(getActivity(), ExhibitorDataList, this, CatName);
                eventAdapter.notifyDataSetChanged();
                exhibitorrecycler.setAdapter(eventAdapter);
                exhibitorrecycler.scheduleLayoutAnimation();
            }


            if (eventrefresh.isRefreshing()) {
                eventrefresh.setRefreshing(false);
            }

        } else {
            Toast.makeText(getActivity(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
            if (eventrefresh.isRefreshing()) {
                eventrefresh.setRefreshing(false);
            }
        }
    }

    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void dismissProgress() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }

    }


    @Override
    public void onContactSelected(ExhibitorDataList eventList) {
        Intent attendeetail = new Intent(getActivity(), ExhibitorDetailActivity.class);
        attendeetail.putExtra("address", eventList.getAddress());
        attendeetail.putExtra("allowedMessage", eventList.getAllowed_message());
        attendeetail.putExtra("allowedsetupMeeting", eventList.getAllowed_setup_meeting());
        attendeetail.putExtra("ExhiCatId", eventList.getExhibitor_category_id());
        attendeetail.putExtra("Exhi_id", eventList.getExhibitor_id());
        attendeetail.putExtra("ExhiLogo", eventList.getLogo());
        attendeetail.putExtra("ExhiName", eventList.getName());
        attendeetail.putExtra("StallNum", eventList.getStall_number());
        attendeetail.putExtra("TileImage", eventList.getTile_image());
        attendeetail.putExtra("CatName", CatName);
        startActivity(attendeetail);

    }

    @Override
    public void onResume() {
        super.onResume();
        sendExhiList(eventid, accesstoken);
    }
}
