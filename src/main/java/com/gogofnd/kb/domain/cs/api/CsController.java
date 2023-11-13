package com.gogofnd.kb.domain.cs.api;

import com.gogofnd.kb.domain.cs.dto.req.InsureHistoryReq;
import com.gogofnd.kb.domain.cs.dto.req.RealTimeCallsReq;
import com.gogofnd.kb.domain.cs.dto.res.InsureHistoryRes;
import com.gogofnd.kb.domain.cs.dto.res.RealTimeCallsRes;
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
}
