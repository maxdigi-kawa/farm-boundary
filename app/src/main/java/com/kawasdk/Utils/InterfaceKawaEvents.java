package com.kawasdk.Utils;

import com.google.gson.JsonObject;

import org.json.JSONObject;

public interface InterfaceKawaEvents {
    public void initKawaMap(boolean isValid);
    public void onkawaSelect(JsonObject data);
    public void onkawaUpdate(JSONObject centerposition);
    public void onkawaSubmit(String data);
    public void onkawaDestroy();

    //public void onkawaUpdate(Double lng, Double lat , Double MAPZOOM);
    //public void showToast(String msg);
}
