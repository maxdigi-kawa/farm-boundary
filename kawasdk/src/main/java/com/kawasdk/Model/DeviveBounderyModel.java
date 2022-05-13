package com.kawasdk.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviveBounderyModel {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("data")
    @Expose
    private Data data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Data getData() {
        return data;
    }
}