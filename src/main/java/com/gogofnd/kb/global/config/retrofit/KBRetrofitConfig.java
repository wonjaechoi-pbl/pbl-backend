package com.gogofnd.kb.global.config.retrofit;

import com.gogofnd.kb.global.config.OkHttpClients;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Slf4j
@NoArgsConstructor
public class KBRetrofitConfig<T>{

    //    101 : 운영
    //    104 : 테스트
    @Value("${app.baseUrl}")
    private String baseUrl;

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private final OkHttpClient httpClient = OkHttpClients.getUnsafeOkHttpClient();

    private retrofit2.Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://10.0.2.104:57509")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build();

    public <T> T create(Class<T> sClass){
        return retrofit.create(sClass);
    }

}