package com.procialize.eventsapp.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.procialize.eventsapp.Activity.DocumentDetailActivity;
import com.procialize.eventsapp.Activity.ExhibitorDetailActivity;
import com.procialize.eventsapp.Activity.LoginActivity;
import com.procialize.eventsapp.Adapter.ExhiDocumentAdapter;
import com.procialize.eventsapp.Adapter.ExhibitorAttendeeAdapter;
import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.DbHelper.ConnectionDetector;
import com.procialize.eventsapp.DbHelper.DBHelper;
import com.procialize.eventsapp.GetterSetter.ExhibitorAttendeeList;
import com.procialize.eventsapp.GetterSetter.ExhibitorBrochureList;
import com.procialize.eventsapp.GetterSetter.ExhibitorCatList;
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

import static android.content.Context.MODE_PRIVATE;

public class DocumentFragment extends Fragment implements ExhiDocumentAdapter.MyTravelAdapterListner {
    RecyclerView attendeerecycler;
    SwipeRefreshLayout attendeefeedrefresh;
    ExhiDocumentAdapter attendeeAdapter;
    List<ExhibitorBrochureList> ExhibitorBrochureList = new ArrayList<>();
    List<ExhibitorBrochureList> ExhibitorAttendeeTemp = new ArrayList<>();
    private List<ExhibitorCatList> ExhibitorCatList = new ArrayList<>();
    private List<ExhibitorDataList> ExhibitorDataList = new ArrayList<>();
    List<ExhibitorAttendeeList> ExhibitorAttendeeList = new ArrayList<>();
    private DBHelper dbHelper;
    private DBHelper procializeDB;
    private SQLiteDatabase db;
    TextView empty;
    String ExhiID;
    String eventid, colorActive, accesstoken;
    String MY_PREFS_NAME = "ProcializeInfo";
    APIService mAPIService;
    ConnectionDetector cd;
    HashMap<String, String> user;
    SessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_document, container, false);


        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");
        colorActive = prefs.getString("colorActive", "");
        attendeerecycler = view.findViewById(R.id.attendeerecycler);
        attendeefeedrefresh = view.findViewById(R.id.attendeefeedrefresh);
        empty = view.findViewById(R.id.empty);
        mAPIService = ApiUtils.getAPIService();
        dbHelper = new DBHelper(getActivity());

        procializeDB = new DBHelper(getActivity());
        db = procializeDB.getWritableDatabase();
        cd = new ConnectionDetector(getActivity());
        session = new SessionManager(getActivity());
        user = session.getUserDetails();
        cd = new ConnectionDetector(getActivity());
        // token
        accesstoken = user.get(SessionManager.KEY_TOKEN);
        ExhibitorDetailActivity exhiD = new ExhibitorDetailActivity();

        ExhiID = ExhibitorDetailActivity.Exhi_id;
        //ExhibitorBrochureList = ExhibitorDetailActivity.ExhibitorBrochureList;
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        attendeerecycler.setLayoutManager(mLayoutManager);

        empty.setTextColor(Color.parseColor(colorActive));
        if (cd.isConnectingToInternet()) {
            sendExhiList(eventid, accesstoken);
        } else {
            ExhibitorAttendeeTemp.clear();
            ExhibitorAttendeeTemp = dbHelper.getBrochureList(ExhiID);
            attendeefeedrefresh.setRefreshing(false);
            if (ExhibitorAttendeeTemp.size() > 0) {
                empty.setVisibility(View.GONE);
                attendeeAdapter = new ExhiDocumentAdapter(getActivity(), ExhibitorAttendeeTemp, DocumentFragment.this);
                attendeeAdapter.notifyDataSetChanged();
                attendeerecycler.setAdapter(attendeeAdapter);
                attendeerecycler.scheduleLayoutAnimation();
            } else {
                empty.setVisibility(View.VISIBLE);
            }
        }


        attendeefeedrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (cd.isConnectingToInternet()) {
                    sendExhiList(eventid, accesstoken);
                } else {
                    ExhibitorAttendeeTemp.clear();
                    ExhibitorAttendeeTemp = dbHelper.getBrochureList(ExhiID);
                    attendeefeedrefresh.setRefreshing(false);
                    if (ExhibitorAttendeeTemp.size() > 0) {
                        empty.setVisibility(View.GONE);
                        attendeeAdapter = new ExhiDocumentAdapter(getActivity(), ExhibitorAttendeeTemp, DocumentFragment.this);
                        attendeeAdapter.notifyDataSetChanged();
                        attendeerecycler.setAdapter(attendeeAdapter);
                        attendeerecycler.scheduleLayoutAnimation();
                    } else {
                        empty.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

        return view;
    }

    @Override
    public void onContactSelected(com.procialize.eventsapp.GetterSetter.ExhibitorBrochureList travel) {

        Intent intent = new Intent(getActivity(), DocumentDetailActivity.class);
        intent.putExtra("type", travel.getFile_type());
        intent.putExtra("file", travel.getFile_name());
        intent.putExtra("br_id", travel.getBrochure_id());
        intent.putExtra("ex_id", travel.getExhibitor_id());
        startActivity(intent);

    }

    public void sendExhiList(String event_id, String token) {
        mAPIService.ExhibitorFetch(event_id, token).enqueue(new Callback<ExhibitorList>() {
            @Override
            public void onResponse(Call<ExhibitorList> call, Response<ExhibitorList> response) {
                if (attendeefeedrefresh.isRefreshing() == true) {
                    attendeefeedrefresh.setRefreshing(false);
                }
                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());
                    if (response.body().getMsg().equalsIgnoreCase("Invalid Token!")) {
                        Intent main = new Intent(getActivity(), LoginActivity.class);
                        startActivity(main);

                    } else {
                        showResponse(response);
                    }
                } else {
                    Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
                    Log.i("hit", "post submitted to API Wrong.");
                }
            }

            @Override
            public void onFailure(Call<ExhibitorList> call, Throwable t) {
                if (attendeefeedrefresh.isRefreshing() == true) {
                    attendeefeedrefresh.setRefreshing(false);
                }
                Toast.makeText(getActivity(), "Low network or no network", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showResponse(Response<ExhibitorList> response) {

        if (response.body().getStatus().equals("success")) {

            ExhibitorDataList = response.body().getExhibitorDataList();
            ExhibitorCatList = response.body().getExhibitorCatList();
            ExhibitorAttendeeList = response.body().getExhibitorAttendeeList();
            ExhibitorBrochureList = response.body().getExhibitorBrochureList();

            dbHelper.clearEXattendeeTable();
            dbHelper.clearEXbrocherTable();
            dbHelper.insertExAttendeeList(ExhibitorAttendeeList, db);
            dbHelper.insertExBrocherList(ExhibitorBrochureList, db);


            ExhiID = ExhibitorDetailActivity.Exhi_id;
            //ExhibitorBrochureList = ExhibitorDetailActivity.ExhibitorBrochureList;

            ExhibitorBrochureList.clear();
            ExhibitorAttendeeTemp.clear();
            ExhibitorBrochureList = dbHelper.getBrochureList(ExhiID);
            for (int i = 0; i < ExhibitorBrochureList.size(); i++) {
                if (ExhibitorBrochureList.get(i).getExhibitor_id().equalsIgnoreCase(ExhiID)) {
                    ExhibitorBrochureList tempExhi = new ExhibitorBrochureList();
                    tempExhi.setBrochure_id(ExhibitorBrochureList.get(i).getBrochure_id());
                    tempExhi.setBrochure_title(ExhibitorBrochureList.get(i).getBrochure_title());
                    tempExhi.setExhibitor_id(ExhibitorBrochureList.get(i).getExhibitor_id());

                    tempExhi.setFile_name(ExhibitorBrochureList.get(i).getFile_name());
                    tempExhi.setFile_type(ExhibitorBrochureList.get(i).getFile_type());
                    tempExhi.setId(ExhibitorBrochureList.get(i).getId());

                    ExhibitorAttendeeTemp.add(tempExhi);
                }
            }

            attendeefeedrefresh.setRefreshing(false);

            if (ExhibitorAttendeeTemp.size() > 0) {
                empty.setVisibility(View.GONE);
                attendeeAdapter = new ExhiDocumentAdapter(getActivity(), ExhibitorAttendeeTemp, DocumentFragment.this);
                attendeeAdapter.notifyDataSetChanged();
                attendeerecycler.setAdapter(attendeeAdapter);
                attendeerecycler.scheduleLayoutAnimation();
            } else {
                empty.setVisibility(View.VISIBLE);
            }


        } else {
            Toast.makeText(getActivity(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
        }
    }
}

