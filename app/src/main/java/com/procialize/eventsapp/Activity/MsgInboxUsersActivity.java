package com.procialize.eventsapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.procialize.eventsapp.Adapter.MeetingUserAdapter;
import com.procialize.eventsapp.Adapter.MsgUserAdapter;
import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.GetterSetter.ExhibitorMeetingUserListing;
import com.procialize.eventsapp.GetterSetter.ExhibitorMsgUserListing;
import com.procialize.eventsapp.GetterSetter.ExhibitorViewUserListing;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;
import com.procialize.eventsapp.Utility.Util;

import java.io.File;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MsgInboxUsersActivity extends AppCompatActivity {
    ImageView headerlogoIv;
    RecyclerView recycler_user;
    LinearLayout linear;
    String MY_PREFS_NAME = "ProcializeInfo";
    String eventid, colorActive, apikey, exhibitor_id, exhibitor_status;
    APIService mAPIService;
    SessionManager sessionManager;
    MsgUserAdapter adapter;
    SwipeRefreshLayout attendeefeedrefresh;
    EditText searchEt;
    TextView txt_header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_inbox_users);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        headerlogoIv = findViewById(R.id.headerlogoIv);
        recycler_user = findViewById(R.id.recycler_user);
        searchEt = findViewById(R.id.searchEt);
        attendeefeedrefresh = findViewById(R.id.attendeefeedrefresh);
        txt_header = findViewById(R.id.txt_header);
        linear = findViewById(R.id.linear);
        Util.logomethod(this, headerlogoIv);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");
        colorActive = prefs.getString("colorActive", "");
        txt_header.setTextColor(Color.parseColor(colorActive));
        mAPIService = ApiUtils.getAPIService();
        sessionManager = new SessionManager(MsgInboxUsersActivity.this);

        HashMap<String, String> user = sessionManager.getUserDetails();

        apikey = user.get(SessionManager.KEY_TOKEN);
        exhibitor_id = user.get(SessionManager.EXHIBITOR_ID);
        exhibitor_status = user.get(SessionManager.EXHIBITOR_STATUS);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recycler_user.setLayoutManager(mLayoutManager);

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

        ExhibitorViewUserListing(eventid, apikey, exhibitor_id);

        attendeefeedrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ExhibitorViewUserListing(eventid, apikey, exhibitor_id);
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
                adapter.getFilter().filter(s.toString());
            }
        });
    }

    public void ExhibitorViewUserListing(String event_id, String token, String exhibitor_id) {
        mAPIService.ExhibitorMsgUserListing(token, event_id, exhibitor_id).enqueue(new Callback<ExhibitorMsgUserListing>() {
            @Override
            public void onResponse(Call<ExhibitorMsgUserListing> call, Response<ExhibitorMsgUserListing> response) {
                if (attendeefeedrefresh.isRefreshing()) {
                    attendeefeedrefresh.setRefreshing(false);
                }
                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());

                    if (response.body().getMsg().equalsIgnoreCase("Invalid Token!")) {
                        Intent main = new Intent(MsgInboxUsersActivity.this, LoginActivity.class);
                        startActivity(main);
                        finish();

                    } else {
                        adapter = new MsgUserAdapter(MsgInboxUsersActivity.this, response.body().getExhibitor_msg_user_list());
                        adapter.notifyDataSetChanged();
                        recycler_user.setAdapter(adapter);


                    }
                } else {
                    if (attendeefeedrefresh.isRefreshing()) {
                        attendeefeedrefresh.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<ExhibitorMsgUserListing> call, Throwable t) {
                Toast.makeText(MsgInboxUsersActivity.this, "Low network or no network", Toast.LENGTH_SHORT).show();
                if (attendeefeedrefresh.isRefreshing()) {
                    attendeefeedrefresh.setRefreshing(false);
                }
            }
        });
    }

}
