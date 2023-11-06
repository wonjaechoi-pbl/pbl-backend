package com.gogofnd.kb.global.config.retrofit;

import com.gogofnd.kb.global.config.OkHttpClients;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KaKaoRetrofitConfig {
    private String baseUrl = "https://biz.service.iwinv.kr";

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();


    private final OkHttpClient httpClient = OkHttpClients.getUnsafeOkHttpClient();

    private retrofit2.Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build();

    public <T> T create(Class<T> sClass){
        return retrofit.create(sClass);
    }


}

