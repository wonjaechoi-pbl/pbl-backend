package com.gogofnd.kb.domain.cs.api;

import com.gogofnd.kb.business.dto.req.AddHistoryReq;
import com.gogofnd.kb.business.dto.req.RiderCsMemoReq;
import com.gogofnd.kb.domain.cs.dto.req.*;
import com.gogofnd.kb.domain.cs.dto.res.*;
import com.gogofnd.kb.domain.cs.service.CsService;
import com.gogofnd.kb.domain.rider.service.RiderService;
import com.gogofnd.kb.global.dto.request.MyPageRequest;
import com.gogofnd.kb.global.dto.response.ApiPagingResponse;
import com.gogofnd.kb.global.dto.response.ApiResponse;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CS 화면을 위한 controller
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/cs")
public class CsController {

    private final CsService csService;
    private final RiderService riderService;

    @GetMapping("/insurance/list")
    @ApiOperation(value = "보험 가입 상태 List 조회")
    public ApiPagingResponse<InsureHistoryRes> InsureHistoryList(
            @RequestParam (defaultValue = "1") int page,
            @RequestParam (defaultValue = "10") int limit,
            InsureHistoryReq req
    ) throws Exception{
        return new ApiPagingResponse<>(csService.selectInsureHistoryList(new MyPageRequest(page,limit).of(), req));
    }

    @GetMapping("/insurance/detail")
    @ApiOperation(value = "보험 가입 상태 이력 List 조회")
    public List<InsureHistoryDetailRes> InsureHistoryDetailList(@RequestParam String loginId) throws Exception{
        return csService.selectInsureHistoryDetailList(loginId);
    }

    @GetMapping("/rider/detail")
    @ApiOperation(value = "라이더 정보 상세 조회")
    public RiderInfoDetailRes RiderInfoDetail(@RequestParam String loginId) throws Exception{
        return csService.selectRiderInfoDetail(loginId);
    }

    @GetMapping("/calls/list")
    @ApiOperation(value = "실시간 운행 이력 List 조회")
    public ApiPagingResponse<RealTimeCallsRes> RealTimeCallsList(
            @RequestParam (defaultValue = "1") int page,
            @RequestParam (defaultValue = "10") int limit,
            RealTimeCallsReq req
    ) throws Exception{
        return new ApiPagingResponse<>(csService.selectRealTimeCallsList(new MyPageRequest(page,limit).of(), req));
    }

    @GetMapping("/calls/settlement/list")
    @ApiOperation(value = "정산 운행 이력 List 조회")
    public ApiPagingResponse<CallsSettlementRes> CallsSettlementList(
            @RequestParam (defaultValue = "1") int page,
            @RequestParam (defaultValue = "10") int limit,
            CallsSettlementReq req
    ) throws Exception{
        return new ApiPagingResponse<>(csService.selectCallsSettlementList(new MyPageRequest(page,limit).of(), req));
    }

    @GetMapping("/accident/list")
    @ApiOperation(value = "사고 이력 List 조회")
    public ApiPagingResponse<AccidentRes> AccidentList(
            @RequestParam (defaultValue = "1") int page,
            @RequestParam (defaultValue = "10") int limit,
            AccidentReq req
    ) throws Exception{
        return new ApiPagingResponse<>(csService.selectAccidentList(new MyPageRequest(page,limit).of(), req));
    }

    @GetMapping("/seller/list")
    @ApiOperation(value = "운영사 List 조회")
    public ApiPagingResponse<SellerRes> SellerList(
            @RequestParam (defaultValue = "1") int page,
            @RequestParam (defaultValue = "10") int limit,
            SellerReq req
    ) throws Exception{
        return new ApiPagingResponse<>(csService.selectSellerList(new MyPageRequest(page,limit).of(), req));
    }

    @GetMapping("/seller/detail")
    @ApiOperation(value = "운영사 정보 상세 조회")
    public SellerInfoDetailRes SellerInfoDetail(@RequestParam String cmpcd) throws Exception{
        return csService.selectSellerInfoDetail(cmpcd);
    }

    @GetMapping("/kb/balance/history/list")
    @ApiOperation(value = "보험사 예납금 이력 List 조회")
    public ApiPagingResponse<KbBalanceHistoryRes> kbBalanceHistoryList(
            @RequestParam (defaultValue = "1") int page,
            @RequestParam (defaultValue = "10") int limit,
            KbBalanceHistoryReq req
    ) throws Exception{
        return new ApiPagingResponse<>(csService.selectKbBalanceHistoryList(new MyPageRequest(page,limit).of(), req));
    }

    @PostMapping("/add/history")
    @ApiOperation(value = "history 추가")
    public ApiResponse<String> addUnderwritingHistory(@RequestBody AddHistoryReq req) {

        return new ApiResponse<>(riderService.addHistory(req));
    }

    @DeleteMapping("/rider/{riderId}")
    @ApiOperation(value="라이더 삭제")
    public ApiResponse<String> deleteRider(@PathVariable Long riderId){
        return new ApiResponse<>(riderService.deleteRider(riderId));
    }

    @PostMapping("/memo/write")
    @ApiOperation(value = "시간제 라이더 메모", notes="시간제 가입 상태 List 화면에서 메모를 작성 할 수 있는 기능")
    public ApiResponse<String> writeRiderCsMemo(@RequestBody RiderCsMemoReq req) throws Exception {
        return new ApiResponse<>(csService.writeCsMemo(req));
    }
}
