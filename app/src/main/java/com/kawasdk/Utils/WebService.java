package com.kawasdk.Utils;

import com.google.gson.JsonObject;
import com.kawasdk.Model.DeviveBounderyModel;
import com.kawasdk.Model.LocationModel.LocationModel;
import com.kawasdk.Model.MergeModel;
import com.kawasdk.Model.PolygonModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WebService {
    @POST("missions")
    Call<DeviveBounderyModel> GET_FARMSs(
            @Header("x-api-key") String x_api_key, @Header("android-version") String android_version,
            @Body JsonObject geoJson
    );
    @GET("signal_bounds/farm_boundaries")
    Call <List<LocationModel>>  getLocations(
    );
    @GET("status/{id}")
    Call<PolygonModel> status(
            @Header("x-api-key") String x_api_key,@Header("android-version") String android_version,
            @Path("id") String id
    );

    @POST("farm_boundaries/merge")
    Call<MergeModel> getMergedPoints(
            @Header("x-api-key") String x_api_key, @Header("android-version") String android_version,
            @Body JsonObject geoJson
    );

    @POST("farm_boundaries/submit")
    Call<MergeModel> sumbitPoints(
            @Header("x-api-key") String x_api_key, @Header("android-version") String android_version,
            @Body JsonObject geoJson
    );

    @GET("reverse/")
    Call<JsonObject> getAddress(
            @Query("format") String json, @Query("lat") String lat, @Query("lon") String lon

    );

    @GET("autocomplete/json")
    Call<JsonObject>  getGoogleLocations(@Query("input") String input,@Query("location") String latlng,@Query("radius") String radius,@Query("key") String key);

    @GET("details/json")
    Call<JsonObject>  getLatLngLocations(@Query("fields") String geometry,@Query("place_id") String latlng,@Query("key") String key);


}

