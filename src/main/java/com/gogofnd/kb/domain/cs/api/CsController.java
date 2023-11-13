package com.gogofnd.kb.domain.cs.api;

import com.gogofnd.kb.domain.cs.dto.req.*;
import com.gogofnd.kb.domain.cs.dto.res.*;
import com.gogofnd.kb.domain.cs.service.CsService;
import com.gogofnd.kb.global.dto.request.MyPageRequest;
import com.gogofnd.kb.global.dto.response.ApiPagingResponse;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

// CS 화면을 위한 controller
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/cs")
public class CsController {

    private final CsService csService;

    @GetMapping("/insurance/list")
    @ApiOperation(value = "보험 가입 상태 List 조회")
    public ApiPagingResponse<InsureHistoryRes> InsureHistoryList(
            @RequestParam (defaultValue = "1") int page,
            @RequestParam (defaultValue = "10") int limit,
            InsureHistoryReq req
    ) throws Exception{
        return new ApiPagingResponse<>(csService.selectInsureHistoryList(new MyPageRequest(page,limit).of(), req));
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
}
