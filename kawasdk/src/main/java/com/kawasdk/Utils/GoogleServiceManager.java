package com.kawasdk.Utils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class GoogleServiceManager {
    private static GoogleServiceManager instance;
    String BASE_URL = "https://maps.googleapis.com/maps/api/place/";

    private WebService webService;

    public static GoogleServiceManager getInstance() {
        if (instance == null) {
            instance = new GoogleServiceManager();
        }
        return instance;
    }

    public GoogleServiceManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        webService = retrofit.create(WebService.class);
    }

    public WebService getKawaService() {
        return webService;
    }
}