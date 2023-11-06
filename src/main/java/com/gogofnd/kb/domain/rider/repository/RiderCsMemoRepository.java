package com.gogofnd.kb.domain.rider.repository;

import com.gogofnd.kb.domain.rider.entity.RiderCsMemo;
import com.gogofnd.kb.domain.rider.entity.RiderInsuranceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderCsMemoRepository extends JpaRepository<RiderCsMemo,Long> {
}
