package com.gogofnd.kb.business;

import com.gogofnd.kb.domain.delivery.dto.insure.req.DeliveryInsureHistoryReq;
import com.gogofnd.kb.domain.delivery.service.DeliveryService;
import com.gogofnd.kb.domain.insurance.dto.*;
import com.gogofnd.kb.domain.insurance.service.InsuranceService;
import com.gogofnd.kb.domain.insurance.service.SignUpService;
import com.gogofnd.kb.domain.insurance.service.UnderWritingService;
import com.gogofnd.kb.domain.rider.dto.res.ResultFormatRes;
import com.gogofnd.kb.domain.rider.service.RiderService;
import com.gogofnd.kb.domain.seller.service.CallService;
import com.gogofnd.kb.business.dto.req.*;
import com.gogofnd.kb.global.dto.response.ApiResponse;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class RetrofitController {

    private final SignUpService signUpService;
    private final InsuranceService insuranceService;
    private final UnderWritingService underWritingService;
    private final RiderService riderService;
    private final DeliveryService deliveryService;
    private final CallService callService;

    // 언더라이팅 요청 . 쓸일 없음. 테스트 용 (전날)
    @PostMapping("/2")
    @ApiOperation(value = "api2 언더라이팅 요청", notes = "전날 앱에서 언더라이팅 요청에 동의한 라이더들의 리스트를 kb측으로 전송합니다.")
    public void api2() throws Exception {
        underWritingService.underWritingRequest();
    }

    @PostMapping("/2/test")
    @ApiOperation(value = "api2 언더라이팅 요청 테스트", notes = "당일 앱에서 언더라이팅 요청에 동의한 라이더들의 리스트를 kb측으로 전송합니다.")
    public void api2Test() throws Exception {
        underWritingService.underWritingRequestTest();
    }

    @PostMapping("/4")
    @ApiOperation(value = "계약체결 이행동의 요청", notes = "당일 언더라이팅 승인된 라이더들에게 계약체결 이행동의 알림톡을 발송합니다.")
    public ResultFormatRes<String> api4() throws Exception {
        return new ResultFormatRes(insuranceService.kbApi4th());
    }

    @GetMapping("/4/specific/{riderId}")
    @ApiOperation(value = "계약체결 이행동의 요청(특정라이더)", notes = "당일 언더라이팅 승인된 특정 라이더 한명에게 계약체결 이행동의 알림톡을 발송합니다.")
    public ApiResponse<String> api4Specific(@PathVariable("riderId") Long riderId) throws Exception {
        return new ApiResponse<>(insuranceService.kbApi4thSpecific(riderId));
    }

    @PostMapping("/5")
    @ApiOperation(value = "api5 기명 요청", notes = "전날 앱에서 기명 요청에 동의한 라이더들의 리스트를 kb측으로 전송합니다.")
    public void api5() throws Exception {
        signUpService.signUpRequest();
    }

    @PostMapping("/7")
    @ApiOperation(value = "api7 기명 취소 요청", notes = "전날 앱에서 기명취소요청에 동의한 라이더들의 리스트를 kb측으로 전송합니다.")
    public void api7() throws Exception {
        riderService.withDrawRiderBatch();
    }

    @PostMapping("/7/test")
    @ApiOperation(value = "api7 기명 취소요청 테스트", notes = "당일 앱에서 기명취소요청에 동의한 라이더들의 리스트를 kb측으로 전송합니다.")
    public void api7Test() throws Exception {
        riderService.withDrawRiderTest();
    }


    //운행 가능여부 조회
    @PostMapping("/9")
    @ApiOperation(value = "api9 운행가능여부조회", notes = "운행을 시작하기전 보험적용 여부확인을 위해 라이더의 정보를 kb측에 전송합니다")
    public ResultDto api9(@RequestBody api9Req api9Req) throws IOException {
        return new ResultDto(callService.kbApi9th(api9Req.getDriver_id()));
    }

    //운행 이력 12번
    @PostMapping("/12")
    @ApiOperation(value = "api12 전체 운행 이력", notes = "전날 하루동안의 라이더 전체 운행 기록을 kb측으로 전송합니다")
    public void api12(){
       deliveryService.getTotalDelivery();
    }

//    @PostMapping("/12/test")
//    @ApiOperation(value = "api12 전체 운행 이력 테스트", notes = "당일 하루동안의 라이더 전체 운행 기록을 kb측으로 전송합니다")
//    public void api12test(){
//        deliveryService.getTotalDeliveryTest();
//    }

//    @PostMapping("/12/test")
//    @ApiOperation(value = "api12 전체 운행 이력 테스트", notes = "당일 하루동안의 라이더 전체 운행 기록을 kb측으로 전송합니다")
//    public CountDto api12Test(@RequestBody DeliveryInsureHistoryReq deliveryInsureHistoryReq) {
//        return CountDto(deliveryService.getTotalDeliveryTest(deliveryInsureHistoryReq.getTotal_time()));
//    }

//    @PostMapping("/13/test")
//    @ApiOperation(value = "api13 특정 운행 이력 테스트", notes = "특정기간 한명의 라이더 운행 기록중 한개의 콜아이디를 kb측으로 전송합니다")
//    public void api13test(String id){
//        deliveryService.getCallIdDeliveryTest(id);
//    }

//    @PostMapping("/14/test")
//    @ApiOperation(value = "api14 특정일 운행 이력 테스트", notes = "특정일 라이더 전체 운행 기록을 kb측으로 전송합니다")
//    public void api14test(@RequestParam("localDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate localDate){
//        deliveryService.getSpecificTotalDeliveryTest(localDate);
//    }

    @PostMapping("/kakao/batch")
    @ApiOperation(value = "카카오톡 안들어간사람한테 일괄 다시쏘기", notes = "가입요청시 카톡 안들어간사람용 한번에 다시보내기")
    public void kakaoTotalBatch(@RequestParam String insuranceStatus) throws Exception {
        insuranceService.kakaoBatch(insuranceStatus);
    }

    @GetMapping("/kakao/batch/2")
    @ApiOperation(value = "카카오톡 안들어간사람한테 일괄 다시쏘기", notes = "가입요청시 카톡 안들어간사람용 한번에 다시보내기")
    public ApiResponse<String> kakaoTotalBatchByDriverId(@RequestParam String driverId) throws Exception {
        return new ApiResponse<>(insuranceService.kakaoBatchByDriverId(driverId));
    }

}
