package com.dozuki.ifixit.guide_create.model;

import android.mtp.MtpStorageInfo;
import android.util.Log;

import java.io.Serializable;

public class ImageObject implements Serializable {

    public int id;
    public String mini;
    public String thumbnail;
    public String standared;
    public String medium;
    public String large;
    public String original;

    public ImageObject() {

    }

    public ImageObject(int id, String mini, String thumbnail, String standard, String medium, String large, String original) {
        this.id = id;
        this.mini = mini.replace("https", "http");
        this.thumbnail = thumbnail.replace("https", "http");
        this.standared = standard.replace("https", "http");
        this.medium = medium.replace("https", "http");
        this.large = large.replace("https", "http");
        this.original = original.replace("https", "http");
    }

}
