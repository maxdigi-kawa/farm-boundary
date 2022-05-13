package com.kawasdk.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PolygonModel implements  Serializable {

    @SerializedName("missionID")
    @Expose
    private String missionID;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("recipeID")
    @Expose
    private String recipeID;

    @SerializedName("data")
    @Expose
    private Data data;

    @SerializedName("response")
    @Expose
    private String response;
    @SerializedName("response_validity")
    @Expose
    private String responseValidity;
    @SerializedName("response_validity_reason")
    @Expose
    private String responseValidityReason;
    @SerializedName("custom_identifier")
    @Expose
    private String customIdentifier;

    public String getMissionID() {
        return missionID;
    }

    public void setMissionID(String missionID) {
        this.missionID = missionID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponseValidity() {
        return responseValidity;
    }

    public void setResponseValidity(String responseValidity) {
        this.responseValidity = responseValidity;
    }

    public String getResponseValidityReason() {
        return responseValidityReason;
    }

    public void setResponseValidityReason(String responseValidityReason) {
        this.responseValidityReason = responseValidityReason;
    }

    public String getCustomIdentifier() {
        return customIdentifier;
    }

    public void setCustomIdentifier(String customIdentifier) {
        this.customIdentifier = customIdentifier;
    }

}
