package com.gogofnd.kb.domain.insurance.api;

import com.gogofnd.kb.domain.insurance.dto.req.GogoSafeSignUpReq;
import com.gogofnd.kb.domain.insurance.dto.req.InitGogosafeDto;
import com.gogofnd.kb.domain.insurance.dto.res.InitGogosafeResDto;
import com.gogofnd.kb.domain.insurance.dto.res.KakaoChannelRes;
import com.gogofnd.kb.global.dto.response.AppResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.HashMap;

public interface GogoSafeApi {
    
    // 센서 동작중인지 리턴
    @GET("/user/sensor/count")
    Call<HashMap<String,Long>> getSensorActiveInfo(
            @Query("phone") String phone
    );

    // 유저 앱 버전 조회 (회원가입할 때 필요해서)
    @GET("/user/app/version")
    Call<AppResponse> getAppVer();

    // 고고세이프 db에 라이더 정보저장되도록 함. (따라서 보험가입할 때 중복가입을 할 필요가 없을 것임.)
    @POST("/auth/signup")
    Call<KakaoChannelRes> gogosafeSignUp(@Body GogoSafeSignUpReq dto);

    @POST("/user/sensor/init")
    Call<InitGogosafeResDto> initGogosafeData(@Body InitGogosafeDto dto);
}
