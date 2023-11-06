package com.gogofnd.kb.domain.delivery.repository;


import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.domain.seller.entity.CallSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface CallSettlementRepository extends JpaRepository<CallSettlement,Long> {
    List<CallSettlement> findAllByCallPickUpTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CallSettlement> findAllByCallPickUpTimeBetweenAndCallCompleteTimeIsNotNull(LocalDateTime start, LocalDateTime end);

    List<CallSettlement> findAllByCallPickUpTimeBetweenAndRiderId(LocalDateTime callPickUpTime, LocalDateTime callPickUpTime2, Long rider_id);
}
