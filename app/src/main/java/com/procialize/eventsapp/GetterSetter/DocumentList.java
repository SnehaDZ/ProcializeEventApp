package com.procialize.eventsapp.GetterSetter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Naushad on 12/11/2017.
 */

public class DocumentList {
    @SerializedName("file_name")
    @Expose
    private String fileName;
    @SerializedName("title")
    @Expose
    private String title;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
