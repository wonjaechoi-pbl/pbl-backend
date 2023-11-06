package com.gogofnd.kb.business;

import com.gogofnd.kb.domain.delivery.dto.accident.AccidentRes;
import com.gogofnd.kb.domain.delivery.service.AccidentService;
import com.gogofnd.kb.domain.delivery.service.DeliveryService;
import com.gogofnd.kb.domain.rider.dto.res.RiderStatusRes;
import com.gogofnd.kb.domain.rider.service.RiderService;
import com.gogofnd.kb.business.dto.req.RiderPhoneReq;
import com.gogofnd.kb.business.dto.res.DailyDrivingRes;
import com.gogofnd.kb.business.dto.res.DailyDrivingTotalRes;
import com.gogofnd.kb.business.dto.res.ResultRes;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


//고고세이프 앱이랑 연동되는 api
@RequestMapping("/app")
@RequiredArgsConstructor
@RestController
public class GogosafeController {
    private final RiderService riderService;
    private final AccidentService accidentService;
    private final DeliveryService deliveryService;


    @PostMapping("/rider_status")
    @ApiOperation(value = "라이더 정보조회", notes = "앱에서 라이더의 정보를 얻기위한 api입니다.")
    public RiderStatusRes getRiderStatus(@Valid @RequestBody RiderPhoneReq dto){
        return riderService.getRiderInsuranceStatus(dto.getPhone());
    }

    @PostMapping("/rider_daily")
    @ApiOperation(value = "라이더 일일 운행 조회", notes = "라이더 일일 운행 조회 api")
    public ResultRes<List<DailyDrivingRes>> findDailyDriving(@Valid @RequestBody RiderPhoneReq req){
        return new ResultRes<>(deliveryService.findDailyDrive(req));
    }

    @PostMapping("/rider_daily_total")
    @ApiOperation(value = "라이더 일일 운행 조회 합계", notes = "라이더 일일 운행 조회 합계 api")
    public DailyDrivingTotalRes findDailyDrivingTotal(@Valid @RequestBody RiderPhoneReq req){
        return deliveryService.findDailyDriveTotal(req);
    }

    @PostMapping("/rider_accident")
    @ApiOperation(value = "사고 접수 현황 api", notes = "앱에서 사고접수.")
    public AccidentRes createAccident(@Valid @RequestBody RiderPhoneReq req){
        return accidentService.findAccidentsByPhone(req.getPhone());
    }
}
