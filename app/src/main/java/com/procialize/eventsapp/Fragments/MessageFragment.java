package com.procialize.eventsapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.procialize.eventsapp.Adapter.MessageAdapter;
import com.procialize.eventsapp.Adapter.NotificationAdapter;
import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.DbHelper.ConnectionDetector;
import com.procialize.eventsapp.DbHelper.DBHelper;
import com.procialize.eventsapp.GetterSetter.Exhibitor_Notification_List;
import com.procialize.eventsapp.GetterSetter.NewsFeedList;
import com.procialize.eventsapp.GetterSetter.NotificationListExhibitorFetch;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MessageFragment extends Fragment implements MessageAdapter.MessageAdapterListner {

    RecyclerView messagelist;
    String MY_PREFS_NAME = "ProcializeInfo";
    String eventid, logoImg, colorActive;
    private APIService mAPIService;
    private DBHelper procializeDB;
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    SessionManager session;
    String accesstoken, exhibitor_status, exhibitor_id;
    HashMap<String, String> user;
    String token, attendee_status;
    ConnectionDetector cd;
    TextView pullrefresh;
    SwipeRefreshLayout notificationRvrefresh;
    TextView msg;

    private List<Exhibitor_Notification_List> newsfeedsDBList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        messagelist = view.findViewById(R.id.notificationRv);
        notificationRvrefresh = view.findViewById(R.id.notificationRvrefresh);
        pullrefresh = view.findViewById(R.id.pullrefresh);
        msg = view.findViewById(R.id.msg);

        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, getActivity().MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");
        logoImg = prefs.getString("logoImg", "");
        colorActive = prefs.getString("colorActive", "");


        procializeDB = new DBHelper(getActivity());
        db = procializeDB.getWritableDatabase();
        dbHelper = new DBHelper(getActivity());
        mAPIService = ApiUtils.getAPIService();
        cd = new ConnectionDetector(getActivity());

        prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, getActivity().MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");
        colorActive = prefs.getString("colorActive", "");
        session = new SessionManager(getActivity());
        user = session.getUserDetails();
        // token
        accesstoken = user.get(SessionManager.KEY_TOKEN);
        exhibitor_id = user.get(SessionManager.EXHIBITOR_ID);
        exhibitor_status = user.get(SessionManager.EXHIBITOR_STATUS);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        messagelist.setLayoutManager(mLayoutManager);
        pullrefresh.setTextColor(Color.parseColor(colorActive));

        SessionManager sessionManager = new SessionManager(getActivity());

        HashMap<String, String> user = sessionManager.getUserDetails();

        // token
        token = user.get(SessionManager.KEY_TOKEN);
        attendee_status = user.get(SessionManager.ATTENDEE_STATUS);

        if (cd.isConnectingToInternet()) {
            NotificationListExhibitorFetch(eventid, accesstoken, exhibitor_id);
        } else {

            newsfeedsDBList.clear();
            newsfeedsDBList = dbHelper.getExNotificationDetails();

            if (newsfeedsDBList.size() == 0) {
                messagelist.setVisibility(View.GONE);
                msg.setVisibility(View.VISIBLE);
            } else {
                messagelist.setVisibility(View.VISIBLE);
                msg.setVisibility(View.GONE);
                MessageAdapter messageAdapter = new MessageAdapter(getActivity(), newsfeedsDBList, new MessageFragment());
                messageAdapter.notifyDataSetChanged();
                messagelist.setAdapter(messageAdapter);
            }
        }

        notificationRvrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (cd.isConnectingToInternet()) {
                    NotificationListExhibitorFetch(eventid, accesstoken, exhibitor_id);
                } else {

                    newsfeedsDBList.clear();
                    newsfeedsDBList = dbHelper.getExNotificationDetails();
                    if (newsfeedsDBList.size() == 0) {
                        messagelist.setVisibility(View.GONE);
                        msg.setVisibility(View.VISIBLE);
                    } else {
                        messagelist.setVisibility(View.VISIBLE);
                        msg.setVisibility(View.GONE);
                        MessageAdapter messageAdapter = new MessageAdapter(getActivity(), newsfeedsDBList, new MessageFragment());
                        messageAdapter.notifyDataSetChanged();
                        messagelist.setAdapter(messageAdapter);
                    }
                }
            }
        });
        return view;
    }

    public void NotificationListExhibitorFetch(String eventid, String token, String exhibitor_id) {

        mAPIService.NotificationListExhibitorFetch(token, eventid, exhibitor_id).enqueue(new Callback<NotificationListExhibitorFetch>() {
            @Override
            public void onResponse(Call<NotificationListExhibitorFetch> call, Response<NotificationListExhibitorFetch> response) {

                if (response.isSuccessful()) {
                    if (notificationRvrefresh.isRefreshing()) {
                        notificationRvrefresh.setRefreshing(false);
                    }
                    dbHelper.clearExNotificationTable();
                    dbHelper.clearEXMeetingTable();
                    dbHelper.clearNotificationTable();
                    dbHelper.insertExNotificationList(response.body().getExhibitor_notification_list(), db);
                    dbHelper.insertNotificationList(response.body().getNotification_list(), db);
                    dbHelper.insertMeetingList(response.body().getExhibitor_meeting_request_list(), db);

                    newsfeedsDBList.clear();
                    newsfeedsDBList = dbHelper.getExNotificationDetails();

                    if (newsfeedsDBList.size() == 0) {
                        messagelist.setVisibility(View.GONE);
                        msg.setVisibility(View.VISIBLE);
                    } else {
                        messagelist.setVisibility(View.VISIBLE);
                        msg.setVisibility(View.GONE);
                        MessageAdapter messageAdapter = new MessageAdapter(getActivity(), newsfeedsDBList, new MessageFragment());
                        messageAdapter.notifyDataSetChanged();
                        messagelist.setAdapter(messageAdapter);
                    }
                } else {
                    if (notificationRvrefresh.isRefreshing()) {
                        notificationRvrefresh.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<NotificationListExhibitorFetch> call, Throwable t) {
                if (notificationRvrefresh.isRefreshing()) {
                    notificationRvrefresh.setRefreshing(false);
                }
            }
        });
    }


    @Override
    public void onContactSelected(Exhibitor_Notification_List travel) {

    }
}
