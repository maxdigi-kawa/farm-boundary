
package com.kawasdk.Model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MergeModel {

    @SerializedName("response")
    @Expose
    private List<ResponseKawa> responseKawa = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<ResponseKawa> getResponse() {
        return responseKawa;
    }

    public void setResponse(List<ResponseKawa> responseKawa) {
        this.responseKawa = responseKawa;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
