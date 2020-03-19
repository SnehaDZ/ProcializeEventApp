package com.procialize.eventsapp.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.procialize.eventsapp.ApiConstant.APIService;
import com.procialize.eventsapp.ApiConstant.ApiConstant;
import com.procialize.eventsapp.ApiConstant.ApiUtils;
import com.procialize.eventsapp.GetterSetter.DirectQuestion;
import com.procialize.eventsapp.GetterSetter.EventSettingList;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;
import com.procialize.eventsapp.Utility.Utility;

import org.apache.commons.lang3.StringEscapeUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class QADirectAdapter extends RecyclerView.Adapter<QADirectAdapter.MyViewHolder> {

    String token, message;
    String QA_like_question, QA_reply_question;
    List<EventSettingList> eventSettingLists;
    String MY_PREFS_NAME = "ProcializeInfo";
    String MY_PREFS_LOGIN = "ProcializeLogin";
    String colorActive;
    private List<DirectQuestion> directQuestionLists;
    private Context context;
    private QADirectAdapterListner listener;
    private String speakername;
    private APIService mAPIService;

    public QADirectAdapter(Context context, List<DirectQuestion> directQuestionLists, QADirectAdapterListner listener) {
        this.directQuestionLists = directQuestionLists;

        this.listener = listener;
        this.context = context;

        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        colorActive = prefs.getString("colorActive", "");


    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.qarow, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final DirectQuestion question = directQuestionLists.get(position);
        holder.nameTv.setTextColor(Color.parseColor(colorActive));

        holder.nameTv.setText(question.getFirst_name());
        holder.QaTv.setText(StringEscapeUtils.unescapeJava(question.getQuestion()));

        holder.AnsTv.setVisibility(View.GONE);

        if (holder.likeLL.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) holder.likeLL.getLayoutParams();
            p.setMargins(5, 15, 5, 5);
            holder.likeLL.requestLayout();
        }

//        if (question.geta()!=null && QA_reply_question.equalsIgnoreCase("1")) {
//            if(!question.getAnswer().equalsIgnoreCase("null")) {
//                holder.AnsTv.setText("Ans :- "+StringEscapeUtils.unescapeJava(question.getAnswer()));
//            }else
//            {
//                holder.AnsTv.setVisibility(View.GONE);
//            }
//        }

        holder.countTv.setText(question.getTotal_likes() + " Likes");

        mAPIService = ApiUtils.getAPIService();

        SessionManager sessionManager = new SessionManager(context);

        HashMap<String, String> user = sessionManager.getUserDetails();

        // token
        token = user.get(SessionManager.KEY_TOKEN);


        eventSettingLists = SessionManager.loadEventList();

        if (eventSettingLists.size() != 0) {
            applysetting(eventSettingLists);
        }


        SimpleDateFormat formatter = null;

        String formate1 = ApiConstant.dateformat;
        String formate2 = ApiConstant.dateformat1;

        if (Utility.isValidFormat(formate1, question.getCreated(), Locale.UK)) {
            formatter = new SimpleDateFormat(ApiConstant.dateformat);
        } else if (Utility.isValidFormat(formate2, question.getCreated(), Locale.UK)) {
            formatter = new SimpleDateFormat(ApiConstant.dateformat1);
        }
        try {
            Date date1 = formatter.parse(question.getCreated());

            DateFormat originalFormat = new SimpleDateFormat("dd MMM HH:mm", Locale.UK);

            String date = originalFormat.format(date1);

            holder.dateTv.setText(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (QA_like_question.equalsIgnoreCase("1")) {
            holder.likeLL.setVisibility(View.VISIBLE);
        } else {
            holder.likeLL.setVisibility(View.GONE);

        }


        if (question.getLike_flag().equals("0")) {
            holder.likeIv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like));
        } else {
            holder.likeIv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_afterlike));
            int colorInt = Color.parseColor(colorActive);
            holder.likeIv.setColorFilter(Color.parseColor(colorActive), PorterDuff.Mode.SRC_ATOP);

        }


    }

    @Override
    public int getItemCount() {
        return directQuestionLists.size();
    }

    private void applysetting(List<EventSettingList> eventSettingLists) {

        for (int i = 0; i < eventSettingLists.size(); i++) {

            if (eventSettingLists.get(i).getFieldName().equals("Q&A_like_question")) {
                QA_like_question = eventSettingLists.get(i).getFieldValue();
            } else if (eventSettingLists.get(i).getFieldName().equals("Q&A_reply_question")) {
                QA_reply_question = eventSettingLists.get(i).getFieldValue();
            }
        }
    }

    public interface QADirectAdapterListner {
        //        void onContactSelected(SpeakerQuestionList question);
//
        void onLikeListener(View v, DirectQuestion question, int position, TextView count, ImageView likeIv);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTv, dateTv, QaTv, AnsTv, countTv;
        public ImageView likeIv;
        LinearLayout likeLL;

        public MyViewHolder(View view) {
            super(view);
            nameTv = view.findViewById(R.id.nameTv);
            dateTv = view.findViewById(R.id.dateTv);
            QaTv = view.findViewById(R.id.QaTv);
            AnsTv = view.findViewById(R.id.AnsTv);
            countTv = view.findViewById(R.id.countTv);
            likeLL = view.findViewById(R.id.likeLL);

            likeIv = view.findViewById(R.id.likeIv);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // send selected contact in callback
//                    listener.onContactSelected(directQuestionLists.get(getAdapterPosition()));
//                }
//            });
//
            likeLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLikeListener(v, directQuestionLists.get(getAdapterPosition()), getAdapterPosition(), countTv, likeIv);
                }
            });
        }
    }
}
