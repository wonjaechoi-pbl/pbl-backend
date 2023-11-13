package com.gogofnd.kb.domain.cs.service;

import com.gogofnd.kb.domain.cs.dto.req.InsureHistoryReq;
import com.gogofnd.kb.domain.cs.dto.res.InsureHistoryRes;
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
    public PagingResponse<InsureHistoryRes> selectInsureHistoryList(Pageable pageable, InsureHistoryReq req) throws Exception {

        return new PagingResponse<>(csRepositorySupport.selectInsureHistoryList(pageable, req));
    }
}
