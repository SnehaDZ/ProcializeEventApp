package com.procialize.eventsapp.InnerDrawerActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.procialize.eventsapp.Adapter.QAAttendeeAdapter;
import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.GetterSetter.AgendaLisQA;
import com.procialize.eventsapp.GetterSetter.AgendaQuestion;
import com.procialize.eventsapp.GetterSetter.QASessionFetch;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;
import com.procialize.eventsapp.Utility.Util;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.jzvd.JZVideoPlayer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QAAttendeeActivity extends AppCompatActivity implements QAAttendeeAdapter.QAAdapterListner {

    public static String Selectedspeaker, SelectedspeakerId;
    public RecyclerView qaRv;
    public Button postbtn;
    public QAAttendeeAdapter qaAttendeeAdapter;
    SwipeRefreshLayout qaRvrefresh;
    ProgressBar progressBar;
    ArrayList<String> list;
    Spinner spinner;
    List<AgendaLisQA> agendaLisQAS;
    Boolean change = true;
    Dialog myDialog;
    String token;
    String MY_PREFS_NAME = "ProcializeInfo";
    String eventid, colorActive;
    ImageView headerlogoIv;
    TextView txtEmpty;
    RelativeLayout linUpper;
    private APIService mAPIService;
    LinearLayout linear;
    TextView txt_header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa);
        //  overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        eventid = prefs.getString("eventid", "1");
        colorActive = prefs.getString("colorActive", "");


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
        TextView header = findViewById(R.id.title);
        header.setTextColor(Color.parseColor(colorActive));


        qaRv = findViewById(R.id.qaRv);
        postbtn = findViewById(R.id.postbtn);
        qaRvrefresh = findViewById(R.id.qaRvrefresh);
        spinner = findViewById(R.id.spinner);
        progressBar = findViewById(R.id.progressBar);
        postbtn.setBackgroundColor(Color.parseColor(colorActive));
        txtEmpty = findViewById(R.id.txtEmpty);
        linUpper = findViewById(R.id.linUpper);
        linear = findViewById(R.id.linear);
        txt_header = findViewById(R.id.txt_header);

        txt_header.setTextColor(Color.parseColor(colorActive));

        try {
//            ContextWrapper cw = new ContextWrapper(HomeActivity.this);
            //path to /data/data/yourapp/app_data/dirName
//            File directory = cw.getDir("/storage/emulated/0/Procialize/", Context.MODE_PRIVATE);
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

        list = new ArrayList<>();
        agendaLisQAS = new ArrayList<>();

        mAPIService = ApiUtils.getAPIService();

        SessionManager sessionManager = new SessionManager(this);

        HashMap<String, String> user = sessionManager.getUserDetails();

        // token
        token = user.get(SessionManager.KEY_TOKEN);


        QAFetch(token, eventid);

        // use a linear layout manager
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        qaRv.setLayoutManager(mLayoutManager);

        int resId = R.anim.layout_animation_slide_right;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, resId);
        //qaRv.setLayoutAnimation(animation);


        qaRvrefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                QAFetch(token, eventid);
            }
        });


        postbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showratedialouge();
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Selectedspeaker = parent.getItemAtPosition(position).toString();
                SelectedspeakerId = agendaLisQAS.get(position).getSessionId();
                QAFetch(token, eventid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void QAFetch(String token, String eventid) {
        showProgress();
        mAPIService.QASessionFetch(token, eventid).enqueue(new Callback<QASessionFetch>() {
            @Override
            public void onResponse(Call<QASessionFetch> call, Response<QASessionFetch> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());

                    if (qaRvrefresh.isRefreshing()) {
                        qaRvrefresh.setRefreshing(false);
                    }
                    dismissProgress();
                    showResponse(response);
                } else {
                    if (qaRvrefresh.isRefreshing()) {
                        qaRvrefresh.setRefreshing(false);
                    }
                    dismissProgress();
                    Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QASessionFetch> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Low network or no network", Toast.LENGTH_SHORT).show();

                dismissProgress();
                if (qaRvrefresh.isRefreshing()) {
                    qaRvrefresh.setRefreshing(false);
                }
            }
        });
    }

    public void showResponse(Response<QASessionFetch> response) {

        // specify an adapter (see also next example)
        if (response.body().getStatus().equalsIgnoreCase("success")) {

            if (myDialog != null) {
                if (myDialog.isShowing()) {
                    myDialog.dismiss();
                }
            }


            if (response.body().getAgendaList().size() != 0 && change == true) {

                change = false;

                agendaLisQAS = response.body().getAgendaList();

                for (int i = 0; i < response.body().getAgendaList().size(); i++) {
                    list.add(response.body().getAgendaList().get(i).getSessionName());
                }

                // Creating adapter for spinner

                ArrayAdapter dataAdapter = new ArrayAdapter(this, R.layout.spinner_item, list) {
                    public View getView(int position, View convertView, ViewGroup parent) {
                        // Cast the spinner collapsed item (non-popup item) as a text view
                        TextView tv = (TextView) super.getView(position, convertView, parent);

                        // Set the text color of spinner item
                        tv.setTextColor(Color.parseColor(colorActive));

                        // Return the view
                        return tv;
                    }
                };
                // Drop down layout style - list view with radio button
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


                // attaching data adapter to spinner
                spinner.setAdapter(dataAdapter);


            }


            if (SelectedspeakerId != null) {
                ArrayList<AgendaQuestion> agendaQuestions = new ArrayList<>();
                for (int j = 0; j < response.body().getAgendaQuestion().size(); j++) {
                    if (SelectedspeakerId.equalsIgnoreCase(response.body().getAgendaQuestion().get(j).getSessionId())) {
                        agendaQuestions.add(response.body().getAgendaQuestion().get(j));
                    }
                }

                if (!(agendaQuestions.isEmpty())) {
                    txtEmpty.setVisibility(View.GONE);
                    // linUpper.setBackground(getResources().getDrawable(R.drawable.close_icon));

                    qaAttendeeAdapter = new QAAttendeeAdapter(QAAttendeeActivity.this, agendaQuestions, response.body().getAgendaList(), this, Selectedspeaker);
                    qaAttendeeAdapter.notifyDataSetChanged();
                    qaRv.setAdapter(qaAttendeeAdapter);
                    qaRv.scheduleLayoutAnimation();
                } else {
                    // linUpper.setBackground(getResources().getDrawable(R.drawable.noqna));

                    txtEmpty.setVisibility(View.VISIBLE);
                    qaAttendeeAdapter = new QAAttendeeAdapter(QAAttendeeActivity.this, agendaQuestions, response.body().getAgendaList(), this, Selectedspeaker);
                    qaAttendeeAdapter.notifyDataSetChanged();
                    qaRv.setAdapter(qaAttendeeAdapter);
                    qaRv.scheduleLayoutAnimation();

                    /*setContentView(R.layout.activity_empty_view);
                    ImageView imageView = findViewById(R.id.back);
                    TextView text_empty = findViewById(R.id.text_empty);
                    text_empty.setText("Q&A not available");
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });*/
                }
            }


        } else {
            Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();

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
    public void onContactSelected(AgendaQuestion question) {

    }

    @Override
    public void onLikeListener(View v, AgendaQuestion question, int position, TextView countTv, ImageView likeIv) {

        if (question.getLikeFlag().equals("1")) {
            likeIv.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_like));
            try {

                int count = Integer.parseInt(question.getTotalLikes());

                if (count > 0) {
                    count = count - 1;
                    countTv.setText(count + " Likes");

                } else {
                    countTv.setText("0" + " Likes");
                }
                QALike(token, eventid, question.getId(), question.getSessionId());
            } catch (Exception e) {
                e.printStackTrace();
            }
//            QAFetch(token, eventid);

        } else {

            likeIv.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_afterlike));
            likeIv.setColorFilter(Color.parseColor(colorActive), PorterDuff.Mode.SRC_ATOP);


            QALike(token, eventid, question.getId(), question.getSessionId());


            try {

                int count = Integer.parseInt(question.getTotalLikes());


                count = count + 1;
                countTv.setText(count + " Likes");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

//        QAFetch(token, eventid);
    }


    private void showratedialouge() {

        myDialog = new Dialog(QAAttendeeActivity.this);
        myDialog.setContentView(R.layout.dialouge_msg_layout);
        myDialog.setCancelable(false);
//        myDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme; //style id

        myDialog.show();

        LinearLayout diatitle = myDialog.findViewById(R.id.diatitle);

        diatitle.setBackgroundColor(Color.parseColor(colorActive));


        Button cancelbtn = myDialog.findViewById(R.id.canclebtn);
        Button ratebtn = myDialog.findViewById(R.id.ratebtn);

        final EditText etmsg = myDialog.findViewById(R.id.etmsg);

        final TextView counttv = myDialog.findViewById(R.id.counttv);
        final TextView nametv = myDialog.findViewById(R.id.nametv);
        final TextView title = myDialog.findViewById(R.id.title);
        final ImageView imgCancel = myDialog.findViewById(R.id.imgCancel);

        cancelbtn.setBackgroundColor(Color.parseColor(colorActive));
        ratebtn.setBackgroundColor(Color.parseColor(colorActive));

        nametv.setTextColor(Color.parseColor(colorActive));


        title.setText("Post Question");


        nametv.setText("To " + Selectedspeaker);

        etmsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                count = 250 - s.length();
                counttv.setText(count + "");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
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

                if (etmsg.getText().toString().length() > 0) {
                    String msg = StringEscapeUtils.escapeJava(etmsg.getText().toString());
                    PostQuetion(token, eventid, msg, Selectedspeaker, SelectedspeakerId);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(QAAttendeeActivity.this);
                    builder.setTitle("");
                    builder.setMessage("Please post a question");

                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                    //
                    // Toast.makeText(getApplicationContext(), "Enter Something", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void PostQuetion(String token, String eventid, String msg, String selectedspeaker, String selectedspeakerId) {
        mAPIService.QASessionPost(token, eventid, msg, selectedspeaker, selectedspeakerId).enqueue(new Callback<QASessionFetch>() {
            @Override
            public void onResponse(Call<QASessionFetch> call, Response<QASessionFetch> response) {

                if (response.isSuccessful()) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    Log.i("hit", "post submitted to API." + response.body().toString());
                    Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();


                    showResponse(response);
                } else {

                    Toast.makeText(QAAttendeeActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<QASessionFetch> call, Throwable t) {
                Toast.makeText(QAAttendeeActivity.this, "Low network or no network", Toast.LENGTH_SHORT).show();

            }
        });
    }


    public void QALike(String token, String eventid, String questionid, String sessionid) {
        mAPIService.QASessionLike(token, eventid, questionid, sessionid).enqueue(new Callback<QASessionFetch>() {
            @Override
            public void onResponse(Call<QASessionFetch> call, Response<QASessionFetch> response) {

                if (response.isSuccessful()) {
                    Log.i("hit", "post submitted to API." + response.body().toString());


                    showLikeResponse(response);
                } else {
                    Toast.makeText(QAAttendeeActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<QASessionFetch> call, Throwable t) {
                Toast.makeText(QAAttendeeActivity.this, "Low network or no network", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void showLikeResponse(Response<QASessionFetch> response) {

        if (response.body().getStatus().equalsIgnoreCase("success")) {
            ArrayList<AgendaQuestion> agendaQuestions = new ArrayList<>();
            for (int j = 0; j < response.body().getAgendaQuestion().size(); j++) {
                if (SelectedspeakerId.equalsIgnoreCase(response.body().getAgendaQuestion().get(j).getSessionId())) {
                    agendaQuestions.add(response.body().getAgendaQuestion().get(j));
                }
            }

            if (!(agendaQuestions.isEmpty())) {
                txtEmpty.setVisibility(View.GONE);
                //linUpper.setBackground(getResources().getDrawable(R.drawable.close_icon));

                qaAttendeeAdapter = new QAAttendeeAdapter(QAAttendeeActivity.this, agendaQuestions, response.body().getAgendaList(), this, Selectedspeaker);
                qaAttendeeAdapter.notifyDataSetChanged();
                qaRv.setAdapter(qaAttendeeAdapter);
                qaRv.scheduleLayoutAnimation();
            } else {
                txtEmpty.setVisibility(View.VISIBLE);
                //linUpper.setBackground(getResources().getDrawable(R.drawable.noqna));

                qaAttendeeAdapter = new QAAttendeeAdapter(QAAttendeeActivity.this, agendaQuestions, response.body().getAgendaList(), this, Selectedspeaker);
                qaAttendeeAdapter.notifyDataSetChanged();
                qaRv.setAdapter(qaAttendeeAdapter);
                qaRv.scheduleLayoutAnimation();

                    /*setContentView(R.layout.activity_empty_view);
                    ImageView imageView = findViewById(R.id.back);
                    TextView text_empty = findViewById(R.id.text_empty);
                    text_empty.setText("Q&A not available");
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });*/
            }

           /* qaAttendeeAdapter = new QAAttendeeAdapter(QAAttendeeActivity.this, agendaQuestions, response.body().getAgendaList(), this, Selectedspeaker);
            qaAttendeeAdapter.notifyDataSetChanged();
            qaRv.setAdapter(qaAttendeeAdapter);*/
//            qaRv.scheduleLayoutAnimation();
//            Toast.makeText(QAAttendeeActivity.this, response.message(), Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(QAAttendeeActivity.this, response.message(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        JZVideoPlayer.releaseAllVideos();

    }
}
