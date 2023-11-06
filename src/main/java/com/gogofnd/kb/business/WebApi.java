package com.gogofnd.kb.business;

import com.gogofnd.kb.business.dto.req.*;
import com.gogofnd.kb.domain.delivery.entity.BalanceHistory;
import com.gogofnd.kb.domain.delivery.repository.BalanceHistoriesRepository;
import com.gogofnd.kb.domain.delivery.repository.CallRepositorySupport;
import com.gogofnd.kb.domain.delivery.service.DeliveryService;
import com.gogofnd.kb.domain.insurance.dto.req.KbApiWebReq;
import com.gogofnd.kb.domain.insurance.service.SignUpService;
import com.gogofnd.kb.domain.insurance.service.WebService;
import com.gogofnd.kb.domain.insurance.service.UnderWritingService;
import com.gogofnd.kb.domain.rider.dto.req.RiderCsReq;
import com.gogofnd.kb.domain.rider.dto.res.ResultFormatRes;
import com.gogofnd.kb.domain.rider.dto.res.RiderCsRes;
import com.gogofnd.kb.domain.rider.dto.res.RiderGogoraRes;
import com.gogofnd.kb.domain.rider.service.RiderService;
import com.gogofnd.kb.global.dto.request.MyPageRequest;
import com.gogofnd.kb.global.dto.response.ApiPagingResponse;
import com.gogofnd.kb.global.dto.response.ApiResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// 운영사들이랑 연동되는 api 다 모아놓은 컨트롤러
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/service")
public class WebApi {

    private final WebService webService;
    private final RiderService riderService;
    private final SignUpService signUpService;
    private final CallRepositorySupport callRepositorySupport;

    @PostMapping("/joinWeb")
    @ApiOperation(value = "가입 요청", notes = "고고에프앤디 드라이버 서비스에 가입한 대리운전 기사에 대해," +
            "보험계약의 인수 여부를 판단하는 심사를 요청하는 API이다")
    public ResultFormatRes<String> joinweb1(@RequestBody KbApiWebReq dto,
                                            @RequestHeader("apikey") String apiKey) throws Exception {
        System.out.println("driver :: " + dto.getDriver_id());
        return new ResultFormatRes(webService.kbRiderWeb(dto,apiKey));
    }

    @PostMapping("/gogora/joinWeb")
    @ApiOperation(value = "가입 요청", notes = "고고에프앤디 드라이버 서비스에 가입한 대리운전 기사에 대해," +
            "보험계약의 인수 여부를 판단하는 심사를 요청하는 API이다")
    public ResultFormatRes<String> joinweb1gogora(KbApiWebReq dto,
                                            @RequestHeader("apikey") String apiKey) throws Exception {
        System.out.println("driver :: " + dto.getDriver_id());
        return new ResultFormatRes(webService.kbRiderWeb(dto,apiKey));
    }

    @PostMapping("/total")
    @ApiOperation(value = "운행 이력 테스트", notes = "라이더 하루 데이터 조회")
    public String getTotalDeliveryWeb(String rider){
        return callRepositorySupport.callRider(rider);
    }

    @PostMapping("/today/total")
    @ApiOperation(value = "오늘자 운행 이력 테스트 (GG)", notes = "라이더 하루 데이터 조회 (GG)")
    public String getTodayTotalDeliveryWeb(String rider){
        return callRepositorySupport.todayRider(rider);
    }

    //이거 거의 배치수준으로 로그로 찍힘 4시간동안 100개가 쌓였더라
//    @PostMapping("/underwrite")
//    @ApiOperation(value = "api2 언더라이팅 요청", notes = "당일 앱에서 언더라이팅 요청에 동의한 라이더들의 리스트를 kb측으로 전송합니다.")
//    public void underwritingTest() throws Exception {
//        underWritingService.underWritingRequestTest();
//    }

    @PostMapping("/signedUrl")
    @ApiOperation(value = "가입동의 url", notes = "url")
    public String signedUrl( @RequestBody InsuranceStatusReq req) throws Exception {
        return webService.kbSignedUrl(req.getDriver_id());
    }

    // 기명요청
    @PostMapping("/5test")
    @ApiOperation(value = "api5 기명 요청", notes = "전날 앱에서 기명 요청에 동의한 라이더들의 리스트를 kb측으로 전송합니다.")
    public void api5test() throws Exception {
        signUpService.signUpRequest();
    }

    @GetMapping
    public String DayInsTest() throws Exception{
        return "";
    }

    @PostMapping("/joinWeb2")
    @ApiOperation(value = "가입 요청 (증권갱신용)", notes = "고고에프앤디 드라이버 서비스에 가입한 대리운전 기사에 대해," +
            "보험계약의 인수 여부를 판단하는 심사를 요청하는 API이다")
    public ResultFormatRes<String> joinweb2(@RequestBody String cmpcd, String riderId,
                                            @RequestHeader("apikey") String apiKey) throws Exception {
        return new ResultFormatRes(webService.kbRiderWeb2(cmpcd,riderId,apiKey));
    }

    @GetMapping("/insurance/list")
    @ApiOperation(value = "시간제 가입 상태 List (고고라 상태 갱신용)", notes="시간제 가입 상태 List 조회 (고고라 상태 갱신용)")
    public List<RiderGogoraRes> riderGoGoraInsuranceList (@RequestParam String cmpcd) throws Exception{
        return riderService.getRiderGoGoraInsuranceList(cmpcd);
    }

    @PutMapping("/update/driverId")
    @ApiOperation(value = "driverId 업데이트", notes="고고라에서 갱신된 driverId를 goplan rider에 update하는 API")
    public void updatedDriverId(@RequestBody UpdateLoginIdReq updateLoginIdReq) throws Exception {
        riderService.putRiderLoginId(updateLoginIdReq);
    }

    @PutMapping("/add/balance")
    @ApiOperation(value = "부릉 balance 추가", notes="고고라에서 충전한 예치금을 부릉 balance에 추가하는 API")
    public String addVroongBalance(@RequestBody AddBalanceReq req) throws Exception {
        return riderService.addVroongBalance(req);
    }

    @PutMapping("/update/rider/status")
    @ApiOperation(value = "라이더 보험적용여부 변경", notes="고고라에서 예치금 충전이 되어있는 경우 Y로 변경, 안 되어있는 경우 N")
    public String updateRiderStatus(@RequestBody UpdateStatusReq req) throws Exception {
        return riderService.updateRiderStatus(req);
    }

    @PostMapping("/update/insureDate")
    @ApiOperation(value = "일보험 보험적용일자 update", notes="고고온에서 라이더가 선택한 보험적용일자를 update하는 API")
    public ApiResponse<String> updateInsureDate(@RequestBody UpdateInsureDateReq req) throws Exception {

        return new ApiResponse<>(riderService.updateInsureDate(req));
    }

    @GetMapping("/check/status")
    @ApiOperation(value = "보험 등재 여부 확인", notes="고고온에서 라이더의 등재여부를 확인하기 위한 API")
    public ApiResponse<String> getCheckInsureStatus(@RequestParam("loginId") String loginId) throws Exception {

        return new ApiResponse<>(riderService.findRiderInsureStatus(loginId));
    }
}
