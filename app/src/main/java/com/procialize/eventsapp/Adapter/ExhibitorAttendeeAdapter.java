package com.procialize.eventsapp.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.procialize.eventsapp.ApiConstant.ApiConstant;
import com.procialize.eventsapp.GetterSetter.ExhibitorAttendeeList;
import com.procialize.eventsapp.GetterSetter.EventSettingList;
import com.procialize.eventsapp.GetterSetter.ExhibitorBrochureList;
import com.procialize.eventsapp.R;
import com.procialize.eventsapp.Session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ExhibitorAttendeeAdapter extends RecyclerView.Adapter<ExhibitorAttendeeAdapter.MyViewHolder> {

    List<EventSettingList> eventSettingLists;
    String attendee_design, attendee_company, attendee_location, attendee_mobile, attendee_save_contact;
    String MY_PREFS_NAME = "ProcializeInfo";
    String MY_PREFS_LOGIN = "ProcializeLogin";
    String colorActive;
    private List<ExhibitorAttendeeList> attendeeLists;
    private Context context;
    private List<ExhibitorAttendeeList> attendeeListFiltered;
    private MyTravelAdapterListner listener;

    public ExhibitorAttendeeAdapter(Context context, List<ExhibitorAttendeeList> attendeeLists, MyTravelAdapterListner listener) {
        this.attendeeLists = attendeeLists;
        this.attendeeListFiltered = attendeeLists;
        this.context = context;
        this.listener = listener;
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        colorActive = prefs.getString("colorActive", "");

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exhi_attendee_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ExhibitorAttendeeList attendee = attendeeListFiltered.get(position);

        SessionManager sessionManager = new SessionManager(context);


        int colorInt = Color.parseColor(colorActive);

        ColorStateList csl = ColorStateList.valueOf(colorInt);
        Drawable drawable = DrawableCompat.wrap(holder.ic_rightarrow.getDrawable());
        DrawableCompat.setTintList(drawable, csl);
        holder.ic_rightarrow.setImageDrawable(drawable);


        if (attendee.getCity() == null) {
            holder.locationTv.setVisibility(View.GONE);
        } else {
            if (attendee.getCity().equalsIgnoreCase("N A")) {
                holder.locationTv.setVisibility(View.GONE);
            }
            if (attendee.getCity().equalsIgnoreCase("")) {
                holder.locationTv.setVisibility(View.GONE);
            }
            if (attendee.getCity().equalsIgnoreCase(" ")) {
                holder.locationTv.setVisibility(View.GONE);
            } else {
                holder.locationTv.setVisibility(View.VISIBLE);
                holder.locationTv.setText(attendee.getCity());
            }
        }


        if(attendee.getFirst_name()==null ){

        }else {
            if (attendee.getFirst_name().equalsIgnoreCase("N A")) {
                holder.nameTv.setText("");
            } else {
                holder.nameTv.setText(attendee.getFirst_name() + " " + attendee.getLast_name());
                holder.nameTv.setTextColor(Color.parseColor(colorActive));
            }
        }


        holder.designationTv.setVisibility(View.GONE);
//        holder.ic_rightarrow.setVisibility(View.GONE);

        if (attendee.getProfile_pic() != null) {


            Glide.with(context).load(ApiConstant.profilepic + attendee.getProfile_pic())
                    .apply(RequestOptions.skipMemoryCacheOf(false))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    holder.profileIv.setImageResource(R.drawable.profilepic_placeholder);
                    return true;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(holder.profileIv);

        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.profileIv.setImageResource(R.drawable.profilepic_placeholder);

        }


    }

    @Override
    public int getItemCount() {
        return attendeeListFiltered.size();
    }


    public interface MyTravelAdapterListner {
        void onContactSelected(ExhibitorAttendeeList travel);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTv, locationTv, designationTv;
        public ImageView profileIv, ic_rightarrow;
        public LinearLayout mainLL;
        public ProgressBar progressBar;

        public MyViewHolder(View view) {
            super(view);
            nameTv = view.findViewById(R.id.nameTv);
            locationTv = view.findViewById(R.id.locationTv);
            designationTv = view.findViewById(R.id.designationTv);

            profileIv = view.findViewById(R.id.profileIV);

            ic_rightarrow = view.findViewById(R.id.ic_rightarrow);

            mainLL = view.findViewById(R.id.mainLL);

            progressBar = view.findViewById(R.id.progressBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onContactSelected(attendeeListFiltered.get(getAdapterPosition()));
                }
            });

        }
    }
}