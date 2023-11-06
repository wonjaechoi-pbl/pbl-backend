package com.gogofnd.kb.domain.rider.service;

import com.gogofnd.kb.business.dto.req.RiderCsMemoReq;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.entity.RiderCsMemo;
import com.gogofnd.kb.domain.rider.repository.RiderCsMemoRepository;
import com.gogofnd.kb.domain.rider.repository.RiderCsMemoRepositorySupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RiderCsService {

    private final RiderCsMemoRepository riderCsMemoRepository;
    private final RiderCsMemoRepositorySupport riderCsMemoRepositorySupport;

    // 라이더 메모 Insert
    public String writeCsMemo(RiderCsMemoReq req) throws Exception {

        Rider rider = riderCsMemoRepositorySupport.findByRiderLoginId(req.getLoginId());
        RiderCsMemo result = RiderCsMemo.create(req, rider);

        riderCsMemoRepository.save(result);

        return "Y";
    }
}
