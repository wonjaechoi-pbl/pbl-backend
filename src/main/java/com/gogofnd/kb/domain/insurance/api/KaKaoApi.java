package com.gogofnd.kb.domain.insurance.api;

import com.gogofnd.kb.domain.insurance.dto.KaKaoChannelApi;
import com.gogofnd.kb.domain.insurance.dto.res.KakaoChannelRes;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface KaKaoApi {
    @POST("/api/send/")
    Call<KakaoChannelRes> kakaoPost(
            @Header("AUTH") String auth,
            @Body KaKaoChannelApi dto);

}
