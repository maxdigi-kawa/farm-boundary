package com.kawasdk.Model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("farms_fetched_at")
    @Expose
    private String farms_fetched_at;

    @SerializedName("boundaries")
    @Expose
    private List<Boundary> boundaries = null;

    public List<Boundary> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(List<Boundary> boundaries) {
        this.boundaries = boundaries;
    }

    public String GET_FARMSs_fetched_at() {
        return farms_fetched_at;
    }

    public void setFarms_fetched_at(String farms_fetched_at) {
        this.farms_fetched_at = farms_fetched_at;
    }

}
