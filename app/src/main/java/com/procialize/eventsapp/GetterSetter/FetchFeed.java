package com.procialize.eventsapp.GetterSetter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Naushad on 11/13/2017.
 */

public class FetchFeed {

    @SerializedName("news_feed_list")
    @Expose
    private List<NewsFeedList> newsFeedList = null;
    @SerializedName("comment_data_list")
    @Expose
    private List<CommentDataList> commentDataList = null;
    @SerializedName("msg")
    @Expose
    private String msg;

    public List<NewsFeedList> getNewsFeedList() {
        return newsFeedList;
    }

    public void setNewsFeedList(List<NewsFeedList> newsFeedList) {
        this.newsFeedList = newsFeedList;
    }

    public List<CommentDataList> getCommentDataList() {
        return commentDataList;
    }

    public void setCommentDataList(List<CommentDataList> commentDataList) {
        this.commentDataList = commentDataList;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
