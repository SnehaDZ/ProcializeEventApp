package com.procialize.eventsapp.InnerDrawerActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.procialize.eventsapp.Adapter.SelfieAdapterNew;
import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.GetterSetter.SelfieList;
import com.procialize.eventsapp.GetterSetter.SelfieListFetch;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;
import com.procialize.eventsapp.Utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.jzvd.JZVideoPlayer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelfieContentActivityNew extends AppCompatActivity {

    Button uploadbtn;
    SwipeRefreshLayout selfiefeedrefresh;
    ProgressBar progressBar;
    GridView gridView;
    String token;
    SelfieAdapterNew selfieAdapter;
    BottomSheetDialog dialog;
    Dialog myDialog;
    List<SelfieList> selfieLists;
    String MY_PREFS_NAME = "ProcializeInfo";
    String eventid;
    ImageView headerlogoIv;
    private APIService mAPIService;
    private Parcelable recyclerViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_new);
        // overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        headerlogoIv = findViewById(R.id.headerlogoIv);
        Util.logomethod(this, headerlogoIv);
        uploadbtn = findViewById(R.id.uploadbtn);
        progressBar = findViewById(R.id.progressBar);
//        selfiefeedrefresh = findViewById(R.id.selfiefeedrefresh);
        gridView = findViewById(R.id.gridview);
        selfieLists = new ArrayList<SelfieList>();

        mAPIService = ApiUtils.getAPIService();

        SessionManager sessionManager = new SessionManager(this);

        HashMap<String, String> user = sessionManager.getUserDetails();

        // token
        token = user.get(SessionManager.KEY_TOKEN);


        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selfie = new Intent(SelfieContentActivityNew.this, SelfieUploadActivity.class);
                startActivity(selfie);
            }
        });
//        selfiefeedrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//
//                SelfieListFetch(token, eventid);
//            }
//        });

        SelfieListFetch(token, eventid);

    }

    public void SelfieListFetch(String token, String eventid) {
        showProgress();
        mAPIService.SelfieListFetch(token, eventid).enqueue(new Callback<SelfieListFetch>() {
            @Override
            public void onResponse(Call<SelfieListFetch> call, Response<SelfieListFetch> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());

//                    if (selfiefeedrefresh.isRefreshing()) {
//                        selfiefeedrefresh.setRefreshing(false);
//                    }
                    dismissProgress();
                    showResponse(response);
                } else {
//                    if (selfiefeedrefresh.isRefreshing()) {
//                        selfiefeedrefresh.setRefreshing(false);
//                    }
                    dismissProgress();
                    Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SelfieListFetch> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Low network or no network", Toast.LENGTH_SHORT).show();

                dismissProgress();
                if (selfiefeedrefresh.isRefreshing()) {
                    selfiefeedrefresh.setRefreshing(false);
                }
            }
        });
    }

    public void showResponse(Response<SelfieListFetch> response) {

        // specify an adapter (see also next example)
        if (response.body().getStatus().equalsIgnoreCase("success")) {

            selfieLists = response.body().getSelfieList();
//            Collections.reverse(selfieLists);
            selfieAdapter = new SelfieAdapterNew(this, selfieLists);
            selfieAdapter.notifyDataSetChanged();
            gridView.setAdapter(selfieAdapter);


        } else {
            Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();

        }
    }

    public void showProgress() {
        if (progressBar.getVisibility() == View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void dismissProgress() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        JZVideoPlayer.releaseAllVideos();

    }
}
