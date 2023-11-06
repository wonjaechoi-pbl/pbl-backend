package com.gogofnd.kb.domain.rider.repository;

import com.gogofnd.kb.domain.rider.entity.RiderInsuranceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderInsuranceHistoryRepository extends JpaRepository<RiderInsuranceHistory,Long> {
    RiderInsuranceHistory findByRiderId(Long riderId);
}
