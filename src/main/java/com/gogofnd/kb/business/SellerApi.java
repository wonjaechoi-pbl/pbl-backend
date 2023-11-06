package com.gogofnd.kb.business;

import com.gogofnd.kb.domain.delivery.service.BalanceService;
import com.gogofnd.kb.domain.delivery.service.DeliveryService;
import com.gogofnd.kb.domain.insurance.dto.req.KbApi1Req;
import com.gogofnd.kb.domain.insurance.service.InsuranceService;
import com.gogofnd.kb.domain.insurance.service.UnderWritingService;
import com.gogofnd.kb.domain.rider.dto.req.DriverIdReq;
import com.gogofnd.kb.domain.rider.dto.res.InsuranceStatusRes;
import com.gogofnd.kb.domain.rider.dto.res.RegistrationStatusRes;
import com.gogofnd.kb.domain.rider.dto.res.ResultFormatRes;
import com.gogofnd.kb.domain.rider.service.RiderService;
import com.gogofnd.kb.domain.seller.dto.Kb10thRequest;
import com.gogofnd.kb.domain.seller.dto.SellerCodeAndDateReq;
import com.gogofnd.kb.domain.seller.dto.SellerCodeReq;
import com.gogofnd.kb.domain.seller.service.CallService;
import com.gogofnd.kb.business.dto.req.*;
import com.gogofnd.kb.business.dto.res.*;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;


// 운영사들이랑 연동되는 api 다 모아놓은 컨트롤러
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/service")
public class SellerApi {

    private final RiderService riderService;
    private final CallService callService;
    private final BalanceService balanceService;
    private final DeliveryService deliveryService;
    private final InsuranceService insuranceService;
    private final UnderWritingService underWritingService;


    @SneakyThrows
    @GetMapping("/test")
    @ApiOperation("가입 요청 테스트입니다.")
    public String test(){
        underWritingService.underWritingRequest();
        return "KB 요청 보냈어용~";
    }
    @PostMapping("/join")
    @ApiOperation(value = "가입 요청(가입설계동의)", notes = "라이더의 정보를 받아서 kb측으로 전달하고, 접속할 웹뷰의 url 값과" +
            "인증을 위한 토큰값을 리턴받습니다.")
    public ResultFormatRes<String> api1(@RequestPart KbApi1Req dto,
                                    @RequestPart(required = false) MultipartFile driver_image,
                                    @RequestHeader("apiKey") String apiKey) throws Exception {
        System.out.println("dto.getDriver_ssn()"+dto.getDriver_ssn());
        return new ResultFormatRes(insuranceService.kbApi1(dto,driver_image,apiKey));
    }

    @PostMapping("/join/cancel")
    @ApiOperation(value = "가입취소요청", notes = "기명취소 요청한 라이더의 정보가 db에 쌓이게 되는 api입니다.")
    public ResultFormatRes<String> api7(@Valid @RequestBody DriverIdReq dto,
                                        @RequestHeader("apiKey") String apiKey) throws Exception {
        return new ResultFormatRes(riderService.sellerWithDrawRider(dto.getDriver_id(),dto.getSeller_code(),apiKey));
    }

    @PostMapping("/status")
    @ApiOperation(value = "가입 상태조회", notes = "라이더 보험상태 현황을 조회하는 api입니다.")
    public InsuranceStatusRes getInsuranceStatus(@Valid @RequestBody InsuranceStatusReq req,
                                                 @RequestHeader("apiKey") String apiKey) throws Exception {
        return riderService.getRiderInsuranceStatus(req.getDriver_id(),req.getSeller_code(),apiKey);
    }

    @PostMapping("/registration/status")
    @ApiOperation(value = "라이더 회원가입 상태조회", notes = "라이더 아이디와 가입상태를 조회하는 api입니다.")
    public RegistrationStatusRes getRiderStatus(@Valid @RequestBody RegistrationStatusReq req,
                                                @RequestHeader("apiKey") String apiKey) throws Exception {
        return riderService.getRiderInsuranceStatus(req,apiKey);
    }

    @PostMapping("/driving/start")
    @ApiOperation(value = "운행시작", notes = "운행 시작을 알리기 위해 라이더의 정보를 kb측에 전송합니다")
    public DrivingStartRes api10(@Valid @RequestBody Kb10thRequest dto,
                                 @RequestHeader("apiKey") String apiKey) throws Exception {
        return callService.kb10th(dto,apiKey);
    }

    //운행 종료
    @PostMapping("/driving/end")
    @ApiOperation(value = "운행종료", notes = "운행 종료를 알리기 위해 라이더의 정보를 kb측에 전송합니다")
    public DrivingEndRes api11(@Valid @RequestBody Kb11thRequest req11,
                               @RequestHeader("apiKey") String apiKey) throws Exception {
        return callService.kb11th(req11,apiKey);
    }

    @PostMapping("/driving/list")
    @ApiOperation(value = "지점별 운행이력 조회", notes = "지점의 72시간 운행이력을 조회합니다.")
    public List<DrivingHistoryResponse> findCallHistoryBySellerCode(@RequestBody SellerCodeAndDateReq dto,
                                                                    @RequestHeader("apiKey") String apiKey) throws Exception {
        return deliveryService.findHistoryBySellerCode(dto,apiKey);
    }

    @PostMapping("/balance")
    @ApiOperation(value = "지점 포인트 조회", notes = "지점의 예납금을 조회합니다")
    public BalanceRes getBalance(@RequestBody BalanceSearch balanceSearch,
                                 @RequestHeader("apiKey") String apiKey) throws Exception {
        return new BalanceRes(balanceService.getBalanceOfSeller(balanceSearch.getSeller_code(),apiKey));
    }

    @PostMapping("/info")
    @ApiOperation(value = "손해율 조회", notes = "손해율, 시간당 요금 조회")
    public RateInfoRes getLossRate(@RequestBody SellerCodeReq dto,
                                   @RequestHeader("apiKey") String apiKey) throws Exception {
        return deliveryService.getRateInfo(dto,apiKey);
    }
//    @PostMapping("/underwrite")
//    @ApiOperation(value="고고세이프 동작여부 확인",notes = "배달대행사에서 고고세이프 동작여부 체크용")
//    public ServiceSensorResDto getGogoSafeStatus(@RequestBody ServiceSensorReqDto dto,@RequestHeader("apiKey") String apiKey) throws Exception {
//        return deliveryService.getGogosafeStatus(dto,apiKey);
//    }
    @GetMapping("/ins/status/list")
    public List<InsuranceStatusListRes> getInsStatusList() {
        return insuranceService.riderInsStatus();
    }
}
