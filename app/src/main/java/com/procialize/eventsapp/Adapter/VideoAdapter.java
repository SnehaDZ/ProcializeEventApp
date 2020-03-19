package com.procialize.eventsapp.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.procialize.eventsapp.CustomTools.RoundCornersTransformation;
import com.procialize.eventsapp.GetterSetter.FirstLevelFilter;
import com.procialize.eventsapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nullable;

import static android.content.Context.MODE_PRIVATE;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    String MY_PREFS_NAME = "ProcializeInfo";
    SharedPreferences prefs;
    String colorActive;
    private List<FirstLevelFilter> videoLists;
    private Context context;
    private VideoAdapterListner listener;

    public VideoAdapter(Context context, List<FirstLevelFilter> galleryLists, VideoAdapterListner listener) {
        this.videoLists = galleryLists;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final FirstLevelFilter videoList = videoLists.get(position);

        holder.nameTv.setText(videoList.getTitle());

        prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        colorActive = prefs.getString("colorActive", "");

        int color = Color.parseColor(colorActive);
        holder.img.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        holder.nameTv.setTextColor(Color.parseColor(colorActive));

        if (videoList.getFolderName() == null) {
            try {
                String CurrentString = videoList.getFileName();
                String[] separated = CurrentString.split("v=");

                String id = separated[1];
//                String url = "https://img.youtube.com/vi/"+id+"/default.jpg";
                String videoId = id.substring(id.lastIndexOf("v=") + 1);
                Log.e("id", videoId);
                String previewURL = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                Log.e("id", previewURL);
                Glide.with(context).load(previewURL)
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(holder.imageIv).onLoadStarted(context.getDrawable(R.drawable.gallery_placeholder));
                holder.progressBar.setVisibility(View.GONE);
//                Picasso.with(context)
//                        .load(previewURL)
//                        .transform(new RoundCornersTransformation(20, 0, false, true))
//                        .placeholder(context.getDrawable(R.drawable.folder_back))
//                        .into(holder.imageIv);
            } catch (Exception e) {
                holder.progressBar.setVisibility(View.GONE);
//                Picasso.with(context)
//                        .load(videoList.getFileName())
//                        .transform(new RoundCornersTransformation(20, 0, false, true))
//                        .placeholder(context.getDrawable(R.drawable.folder_back))
//                        .into(holder.imageIv);

                Glide.with(context).load(videoList.getFileName())
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(holder.imageIv).onLoadStarted(context.getDrawable(R.drawable.gallery_placeholder));
            }
        } else {
            holder.imageIv.setBackgroundResource(R.drawable.folder_back);
            holder.progressBar.setVisibility(View.GONE);
//            Picasso.with(context)
//                    .load(videoList.getFileName())
//                    .transform(new RoundCornersTransformation(20, 0, false, true))
//                    .placeholder(context.getDrawable(R.drawable.folder_back))
//                    .into(holder.imageIv);

            Glide.with(context).load(videoList.getFileName())
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(holder.imageIv).onLoadStarted(context.getDrawable(R.drawable.gallery_placeholder));
        }


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return videoLists.size();
    }

    public interface VideoAdapterListner {
        void onContactSelected(FirstLevelFilter firstLevelFilter);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTv;
        public ImageView imageIv, img;
        public LinearLayout mainLL;
        private ProgressBar progressBar;


        public MyViewHolder(View view) {
            super(view);
            nameTv = view.findViewById(R.id.nameTv);
            imageIv = view.findViewById(R.id.imageIv);
            img = view.findViewById(R.id.img);
            mainLL = view.findViewById(R.id.mainLL);
            progressBar = view.findViewById(R.id.progressBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onContactSelected(videoLists.get(getAdapterPosition()));
                }
            });
        }
    }
}