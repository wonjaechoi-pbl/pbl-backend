package com.gogofnd.kb.domain.cs.service;

import com.gogofnd.kb.domain.cs.dto.req.*;
import com.gogofnd.kb.domain.cs.dto.res.*;
import com.gogofnd.kb.domain.cs.repository.CsRepositorySupport;
import com.gogofnd.kb.global.dto.response.PagingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CsService {
    private final CsRepositorySupport csRepositorySupport;

    // 보험 가입 상태 List 조회
    public PagingResponse<InsureHistoryRes> selectInsureHistoryList(Pageable pageable, InsureHistoryReq req) throws Exception {

        return new PagingResponse<>(csRepositorySupport.selectInsureHistoryList(pageable, req));
    }

    // 실시간 운행 이력 List 조회
    public PagingResponse<RealTimeCallsRes> selectRealTimeCallsList(Pageable pageable, RealTimeCallsReq req) throws Exception {

        return new PagingResponse<>(csRepositorySupport.selectRealTimeCallsList(pageable, req));
    }

    // 정산 운행 이력 List 조회
    public PagingResponse<CallsSettlementRes> selectCallsSettlementList(Pageable pageable, CallsSettlementReq req) throws Exception {

        return new PagingResponse<>(csRepositorySupport.selectCallsSettlementList(pageable, req));
    }

    // 사고 이력 List 조회
    public PagingResponse<AccidentRes> selectAccidentList(Pageable pageable, AccidentReq req) throws Exception {

        return new PagingResponse<>(csRepositorySupport.selectAccidentList(pageable, req));
    }

    // 운영사 List 조회
    public PagingResponse<SellerRes> selectSellerList(Pageable pageable, SellerReq req) throws Exception {

        return new PagingResponse<>(csRepositorySupport.selectSellerList(pageable, req));
    }
}
