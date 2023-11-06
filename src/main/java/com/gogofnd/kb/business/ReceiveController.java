package com.gogofnd.kb.business;

import com.gogofnd.kb.domain.delivery.dto.accident.AccidentCreate;
import com.gogofnd.kb.domain.delivery.dto.insure.req.BalanceInsureReq;
import com.gogofnd.kb.domain.delivery.dto.insure.req.DeliveryInsureHistoryReq;
import com.gogofnd.kb.domain.delivery.dto.insure.res.DeliveryInsureAccidentResponseDto;
import com.gogofnd.kb.domain.delivery.dto.insure.res.DeliveryInsureHistorykbResponseDto;
import com.gogofnd.kb.domain.delivery.dto.insure.res.ResultDto;
import com.gogofnd.kb.domain.delivery.service.AccidentService;
import com.gogofnd.kb.domain.delivery.service.BalanceService;
import com.gogofnd.kb.domain.delivery.service.DeliveryService;
import com.gogofnd.kb.domain.insurance.dto.req.KbApi8thReq;
import com.gogofnd.kb.domain.insurance.dto.KbApiSignResultDto;
import com.gogofnd.kb.domain.insurance.dto.res.KbApiUnderWritingResult;
import com.gogofnd.kb.domain.insurance.service.SignUpService;
import com.gogofnd.kb.domain.insurance.service.UnderWritingService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//kb에서 데이터 수신하는 api
@RequestMapping("/insure")
@RequiredArgsConstructor
@RestController
public class ReceiveController {
    private final UnderWritingService underWritingService;
    private final SignUpService signUpService;
    private final AccidentService accidentService;
    private final BalanceService balanceService;
    private final DeliveryService deliveryService;

    @PostMapping("/underwriting/result")
    @ApiOperation(value = "api3 언더라이팅 결과 수신", notes = "라이더들의 언더라이팅 결과를 리스트 형태로 kb로부터 수신합니다. " +
            "고고에프앤디 서버는 카운트를 리턴합니다.")
    public ResponseEntity api3(@RequestBody List<KbApiUnderWritingResult> dto){
        return new ResponseEntity(underWritingService.underWritingResult(dto),HttpStatus.OK);
    }

    @PostMapping("/signup/result")
    @ApiOperation(value = "api6 기명요청 결과 수신", notes = "라이더들의 기명요청 결과를 리스트 형태로 kb로부터 수신합니다. " +
            "고고에프앤디 서버는 카운트를 리턴합니다.")
    public ResponseEntity api6(@RequestBody List<KbApiSignResultDto> dtoList){
        return new ResponseEntity(signUpService.signUpResult(dtoList),HttpStatus.OK);
    }

    @PostMapping("/signup/cancel")
    @ApiOperation(value = "api8 기명취소요청 결과 수신", notes = "라이더들의 기명취소 요청 심사결과를 리스트 형태로 kb로부터 수신합니다. " +
            "고고에프앤디 서버는 카운트를 리턴합니다.")
    public ResponseEntity api8(@RequestBody List<KbApi8thReq> dtoList){
        return new ResponseEntity(signUpService.kbApi8th(dtoList),HttpStatus.OK);
    }

//    @PostMapping("/payment")
//    @ApiOperation(value = "api13 예납금 잔액 조회", notes = "kb측에서 운행이력을 바탕으로 차감된 예납금을 고고에프앤디에게 송신합니다")
//    public ResultDto deliveryPayment(@RequestBody BalanceInsureReq balanceInsureReq){
//
//        ResultDto resultDto = balanceService.balanceTotal(balanceInsureReq);
//        return resultDto;
//    }

    @PostMapping("/payment")
    @ApiOperation(value="api13 예납금 잔액 조회 (리스트)", notes = "kb측에서 운행이력을 바탕으로 차감된 예납금을 고고에프앤디에게 송신합니다")
    public ResultDto deliveryPaymentList(@RequestBody List<BalanceInsureReq> balanceInsureReqList){
        return balanceService.balanceListTotal(balanceInsureReqList);
    }

    @PostMapping("/bikehistory")
    @ApiOperation(value = "api 14 운행 상세이력조회", notes = "kb측에서 라이더 정보를 고고에프앤디로 전송하면 해당 라이더의 72시간동안 모든 운행정보를 리턴합니다")
    public List<DeliveryInsureHistorykbResponseDto> deliveryHistory(@RequestBody DeliveryInsureHistoryReq DeliveryInsureHistoryReq){
        return deliveryService.historyKb(DeliveryInsureHistoryReq);
    }

    @PostMapping("/accident")
    @ApiOperation(value = "api15 사고접수 고지", notes = "kb로부터 사고접수 받은 콜의 배차시간과 연관된 콜들을 리스트형태로 리턴합니다.")
    public List<DeliveryInsureAccidentResponseDto> getAccidents(@RequestBody AccidentCreate dto){
        return accidentService.findAccidents(dto);
    }
}
