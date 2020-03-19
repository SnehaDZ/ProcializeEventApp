package com.procialize.eventsapp.Activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.GetterSetter.Analytic;
import com.procialize.eventsapp.GetterSetter.EventSettingList;
import com.procialize.eventsapp.GetterSetter.RatingSessionPost;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;
import com.procialize.eventsapp.Utility.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.procialize.eventsapp.Utility.Utility.setgradientDrawable;

public class AgendaDetailActivity extends AppCompatActivity {

    String agendaid, date, name, description, starttime, endtime;
    TextView tvname, tvdate, tvtime, tvdscription;
    Button ratebtn;
    APIService mAPIService;
    SessionManager sessionManager;
    String apikey;
    float ratingval;
    Dialog myDialog;
    String agenda_save_to_calendar, agenda_text_description;
    List<EventSettingList> eventSettingLists;
    TextView msg;
    String MY_PREFS_NAME = "ProcializeInfo";
    String eventid, colorActive;
    LinearLayout linear1, linear2, linear3;
    View viewtwo, viewone;
    RatingBar ratingbar;
    ProgressBar progressBar;
    ImageView headerlogoIv;
    String spitdate;
    String startdatestr;
    String enddatestr;
    RelativeLayout relative;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_detail);
        //overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        //Testing purpose

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

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");
        colorActive = prefs.getString("colorActive", "");


        mAPIService = ApiUtils.getAPIService();
        sessionManager = new SessionManager(AgendaDetailActivity.this);

        HashMap<String, String> user = sessionManager.getUserDetails();

        // apikey
        apikey = user.get(SessionManager.KEY_TOKEN);

        eventSettingLists = SessionManager.loadEventList();

        if (eventSettingLists.size() != 0) {
            applysetting(eventSettingLists);
        }

        SubmitAnalytics(apikey, eventid, "", "", "agendaDetail");

        try {
            agendaid = getIntent().getExtras().getString("id");
            name = getIntent().getExtras().getString("name");
            date = getIntent().getExtras().getString("date");
            description = getIntent().getExtras().getString("description");
            starttime = getIntent().getExtras().getString("starttime");
            endtime = getIntent().getExtras().getString("endtime");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Typeface typeFace = Typeface.createFromAsset(getAssets(),
                "DINPro-Regular.ttf");

        tvname = findViewById(R.id.tvname);
        tvdate = findViewById(R.id.tvdate);
        tvtime = findViewById(R.id.tvtime);
        tvdscription = findViewById(R.id.tvdscription);
        linear1 = findViewById(R.id.linear1);
        linear2 = findViewById(R.id.linear2);
        linear3 = findViewById(R.id.linear3);
        viewone = findViewById(R.id.viewone);
        viewtwo = findViewById(R.id.viewtwo);
        ratebtn = findViewById(R.id.ratebtn);
        ratingbar = findViewById(R.id.ratingbar);
        progressBar = findViewById(R.id.progressBar);
        msg = findViewById(R.id.msg);
        relative = findViewById(R.id.relative);

        tvname.setBackgroundColor(Color.parseColor(colorActive));

        try {

            File mypath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Procialize/" + "background.jpg");
            Resources res = getResources();
            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(mypath));
            BitmapDrawable bd = new BitmapDrawable(res, bitmap);
            relative.setBackgroundDrawable(bd);

            Log.e("PATH", String.valueOf(mypath));
        } catch (Exception e) {
            e.printStackTrace();
            relative.setBackgroundColor(Color.parseColor("#f1f1f1"));

        }


        GradientDrawable shape = setgradientDrawable(5, colorActive);
        ratebtn.setBackground(shape);

        tvdscription.setTypeface(typeFace);
        tvdscription.setMovementMethod(new ScrollingMovementMethod());

        LayerDrawable stars = (LayerDrawable) ratingbar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor(colorActive),
                PorterDuff.Mode.SRC_ATOP);

        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingval = rating;
            }
        });

        ratebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ratingval > 0) {

                    String rate = String.valueOf(ratingval);

                    PostRate(eventid, rate, apikey, agendaid);
                } else {
                    Toast.makeText(AgendaDetailActivity.this, "Please rate on a scale of 1-5 stars", Toast.LENGTH_SHORT).show();

                }
            }
        });

        if (name != null) {
            tvname.setText(name);
        } else {
            tvname.setVisibility(View.GONE);

        }

        if (date != null) {
            try {
                SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
                SimpleDateFormat targetFormat = new SimpleDateFormat("dd MMMM");

                Date ogdate = originalFormat.parse(date);
                String day = String.valueOf(android.text.format.DateFormat.format("EEEE", ogdate));
                String sessiondate = targetFormat.format(ogdate);
                tvdate.setText(sessiondate + " - " + day);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tvdate.setVisibility(View.GONE);
            linear1.setVisibility(View.GONE);
            viewone.setVisibility(View.GONE);
        }


        if (starttime != null && endtime != null) {
            try {
                SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.UK);
                SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm ");
                SimpleDateFormat datesplit = new SimpleDateFormat("dd-MM-yyyy");

                Date startdate = originalFormat.parse(starttime);
                Date enddate = originalFormat.parse(endtime);

//                String startdatestr = targetFormat.format(startdate);
//                String enddatestr = targetFormat.format(enddate);
                startdatestr = targetFormat.format(startdate);
                enddatestr = targetFormat.format(enddate);
                spitdate = datesplit.format(startdate);
                tvtime.setText(startdatestr + " - " + enddatestr);

            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            tvtime.setVisibility(View.GONE);
            linear2.setVisibility(View.GONE);
            viewone.setVisibility(View.GONE);
        }

        if (description != null) {
            if (description.equalsIgnoreCase("")) {
                tvdscription.setVisibility(View.GONE);
                linear3.setVisibility(View.GONE);
                viewtwo.setVisibility(View.GONE);
            } else if (description.equalsIgnoreCase(" ")) {
                tvdscription.setVisibility(View.GONE);
                linear3.setVisibility(View.GONE);
                viewtwo.setVisibility(View.GONE);
            } else {
                tvdscription.setText(description);
            }
        } else {
            tvdscription.setVisibility(View.GONE);
            linear3.setVisibility(View.GONE);
            viewtwo.setVisibility(View.GONE);
        }

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 15);
        Date teenMinutesFromsave = now.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = df.format(teenMinutesFromsave);
        SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm aa");
        SimpleDateFormat datesplit = new SimpleDateFormat("dd-MM-yyyy");

        String currenttime = targetFormat.format(teenMinutesFromsave);
        String currentdate = datesplit.format(teenMinutesFromsave);




    }

    private void applysetting(List<EventSettingList> eventSettingLists) {

        for (int i = 0; i < eventSettingLists.size(); i++) {

            if (eventSettingLists.get(i).getFieldName().equals("agenda_save_to_calendar")) {
                agenda_save_to_calendar = eventSettingLists.get(i).getFieldValue();
            } else if (eventSettingLists.get(i).getFieldName().equals("agenda_text_description")) {
                agenda_text_description = eventSettingLists.get(i).getFieldValue();
            }
        }
    }


    private void showratedialouge() {

        myDialog = new Dialog(AgendaDetailActivity.this);
        myDialog.setContentView(R.layout.dialog_rate_layout);
        myDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme; //style id

        myDialog.show();

        Button cancelbtn = myDialog.findViewById(R.id.canclebtn);
        Button ratebtn = myDialog.findViewById(R.id.ratebtn);

        RatingBar ratingBar = myDialog.findViewById(R.id.ratingbar);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingval = rating;
            }
        });

        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        ratebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ratingval > 0) {

                    String rate = String.valueOf(ratingval);

                    PostRate(eventid, rate, apikey, agendaid);
                } else {
                    Toast.makeText(AgendaDetailActivity.this, "Please rate on a scale of 1-5 stars", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    public void PostRate(String eventid, String rating, String token, String speakerid) {
//        showProgress();
//        showProgress();
        mAPIService.RatingSessionPost(token, eventid, speakerid, rating, "", "session").enqueue(new Callback<RatingSessionPost>() {
            @Override
            public void onResponse(Call<RatingSessionPost> call, Response<RatingSessionPost> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());
//                    dismissProgress();
                    ratingbar.setRating(0F);
//                    dismissProgress();
                    DeletePostresponse(response);
                } else {
//                    dismissProgress();
//                    dismissProgress();
                    Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RatingSessionPost> call, Throwable t) {
                Log.e("hit", "Low network or no network");
                Log.e("hit", t.getMessage());
//                dismissProgress();
                Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_SHORT).show();
//                dismissProgress();
            }
        });
    }

    private void DeletePostresponse(Response<RatingSessionPost> response) {

        if (response.body() != null) {
            if (response.body().getStatus().equalsIgnoreCase("Success")) {

                Log.e("post", "success");

//            myDialog.dismiss();
                Toast.makeText(this, response.body().getMsg(), Toast.LENGTH_SHORT).show();

                SubmitAnalytics(apikey, eventid, "", "", "rating");
            } else {
                Log.e("post", "fail");
//            myDialog.dismiss();
//            Toast.makeText(this,response.body().getMsg(),Toast.LENGTH_SHORT).show();
            }
        } else {

        }
    }

    @Override
    protected void onResume() {
        //overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        super.onResume();
    }

    public void SubmitAnalytics(String token, String eventid, String target_attendee_id, String target_attendee_type, String analytic_type) {

        mAPIService.Analytic(token, eventid, target_attendee_id, target_attendee_type, analytic_type).enqueue(new Callback<Analytic>() {
            @Override
            public void onResponse(Call<Analytic> call, Response<Analytic> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "Analytics Sumbitted" + response.body().toString());


                } else {

//                    Toast.makeText(AgendaDetailActivity.this, "Unable to process", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Analytic> call, Throwable t) {
//                Toast.makeText(AgendaDetailActivity.this, "Unable to process", Toast.LENGTH_SHORT).show();

            }
        });
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
}
