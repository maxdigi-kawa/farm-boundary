
package com.kawasdk.Model.LocationModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationModel {

    @SerializedName("ID")
    @Expose
    private String id;
    @SerializedName("RecipeIdentifier")
    @Expose
    private String recipeIdentifier;
    @SerializedName("PointExpansionRadius")
    @Expose
    private Integer pointExpansionRadius;
    @SerializedName("IsWorldwide")
    @Expose
    private Boolean isWorldwide;
    @SerializedName("RecipeBoundsPolygon")
    @Expose
    private RecipeBoundsPolygon recipeBoundsPolygon;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipeIdentifier() {
        return recipeIdentifier;
    }

    public void setRecipeIdentifier(String recipeIdentifier) {
        this.recipeIdentifier = recipeIdentifier;
    }

    public Integer getPointExpansionRadius() {
        return pointExpansionRadius;
    }

    public void setPointExpansionRadius(Integer pointExpansionRadius) {
        this.pointExpansionRadius = pointExpansionRadius;
    }

    public Boolean getIsWorldwide() {
        return isWorldwide;
    }

    public void setIsWorldwide(Boolean isWorldwide) {
        this.isWorldwide = isWorldwide;
    }

    public RecipeBoundsPolygon getRecipeBoundsPolygon() {
        return recipeBoundsPolygon;
    }

    public void setRecipeBoundsPolygon(RecipeBoundsPolygon recipeBoundsPolygon) {
        this.recipeBoundsPolygon = recipeBoundsPolygon;
    }

}