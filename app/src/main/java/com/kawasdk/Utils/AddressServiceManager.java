package com.kawasdk.Utils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class AddressServiceManager {
    private static AddressServiceManager instance;

    private WebService webService;

    public static AddressServiceManager getInstance() {
        if (instance == null) {
            instance = new AddressServiceManager();
        }
        return instance;
    }

    public AddressServiceManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.ADRESS_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        webService = retrofit.create(WebService.class);
    }

    public WebService getKawaService() {
        return webService;
    }
}