package com.gogofnd.kb.domain.rider.service;

import com.gogofnd.kb.business.dto.req.RiderCsMemoReq;
import com.gogofnd.kb.domain.rider.dto.req.RiderCsReq;
import com.gogofnd.kb.domain.rider.dto.res.RiderCsRes;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.entity.RiderCsMemo;
import com.gogofnd.kb.domain.rider.repository.RiderCsMemoRepository;
import com.gogofnd.kb.domain.rider.repository.RiderCsMemoRepositorySupport;
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
public class RiderCsService {

    private final RiderCsMemoRepository riderCsMemoRepository;
    private final RiderCsMemoRepositorySupport riderCsMemoRepositorySupport;

    // 라이더 가입상태 List 조회
    public PagingResponse<RiderCsRes> getRiderCsList(Pageable pageable, RiderCsReq riderCsReq) throws Exception {

        return new PagingResponse<>(riderCsMemoRepositorySupport.findRiderCsList(pageable, riderCsReq));
    }
    
    // 라이더 메모 Insert
    public String writeCsMemo(RiderCsMemoReq req) throws Exception {

        Rider rider = riderCsMemoRepositorySupport.findByRiderLoginId(req.getLoginId());
        RiderCsMemo result = RiderCsMemo.create(req, rider);

        riderCsMemoRepository.save(result);

        return "Y";
    }
}
