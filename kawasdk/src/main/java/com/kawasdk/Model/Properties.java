
package com.kawasdk.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("area")
    @Expose
    private Float area;

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
    }

}
