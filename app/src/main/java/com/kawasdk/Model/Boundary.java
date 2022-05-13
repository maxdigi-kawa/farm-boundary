package com.kawasdk.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Boundary {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("properties")
    @Expose
    private Properties properties;

    @SerializedName("geojson")
    @Expose
    private Geojson geojson;

    @SerializedName("edited_by_user")
    @Expose
    private Boolean editedByUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Geojson getGeojson() {
        return geojson;
    }

    public void setGeojson(Geojson geojson) {
        this.geojson = geojson;
    }

    public Boolean getEditedByUser() {
        return editedByUser;
    }

    public void setEditedByUser(Boolean editedByUser) {
        this.editedByUser = editedByUser;
    }

}
