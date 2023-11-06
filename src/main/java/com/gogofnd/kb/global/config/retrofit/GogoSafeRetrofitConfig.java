package com.gogofnd.kb.global.config.retrofit;

import com.gogofnd.kb.global.config.OkHttpClients;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GogoSafeRetrofitConfig {
//    private String baseUrl = "http://cg.navers.co.kr:8081";
//    private String baseUrl = "http://116.120.58.222:8081"; // cg서버 IP로 일단 활용
//    private String baseUrl = "http://112.175.41.185:8081";

//  private String baseUrl = "http://112.175.41.179:9888";
private String baseUrl = "http://116.120.58.222:8081";

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();


    private final OkHttpClient httpClient = OkHttpClients.getUnsafeOkHttpClient();

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build();

    public <T> T create(Class<T> sClass){
        return retrofit.create(sClass);
    }


}

