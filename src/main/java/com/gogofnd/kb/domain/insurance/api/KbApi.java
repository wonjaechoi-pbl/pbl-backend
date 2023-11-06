package com.gogofnd.kb.domain.insurance.api;


import com.gogofnd.kb.domain.delivery.dto.insure.req.DeliveryInsureHistoryReq;
import com.gogofnd.kb.domain.insurance.dto.*;

import com.gogofnd.kb.domain.insurance.dto.req.*;
import com.gogofnd.kb.domain.insurance.dto.res.KbApi4thRes;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.util.List;

public interface KbApi {
    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: design_agreement_url_requests"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<TokenAndUrlRes> kbApi1Retrofit(@Body KbDto dto);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: underwriting_requests"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<CountDto> kbApi2Retrofit(@Body List<KbApiRiderDto> dto);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: agreement_url_requests"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<KbApi4thRes> kbApi4Retrofit(@Body KbApi4thReq dto);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: endorsements_requests"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<CountDto> kbApi5Retrofit(@Body List<KbSignUpReq> dto);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: endorsements_cancel_requests"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<CountDto> kbApi7Retrofit(@Body List<KbSignUpReq> requests);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: driving_effective_inquiry"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<ResultDto> kbApi9Retrofit(@Body KbApi9thReq dto);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: driving_start"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<ResultDto> kbApi10Retrofit(@Body KbApi10thReq dto);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: driving_end"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<ResultDto> kbApi11Retrofit(@Body KbApi11Req dto);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: driving_list"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<com.gogofnd.kb.domain.delivery.dto.insure.res.CountDto> kbApi12Retrofit(@Body List<DeliveryInsureHistoryReq> dto);

    @Headers({
            "Accept: application/json",
            "X-WHEEL-INSURANCE-URI: design_agreement_url_requests"
    })
    @POST("/gogofnd/kbInsOnline.do")
    Call<TokenAndUrlRes> kbRiderWebRetrfit(@Body KbApiRiderWebDto dto);
}
