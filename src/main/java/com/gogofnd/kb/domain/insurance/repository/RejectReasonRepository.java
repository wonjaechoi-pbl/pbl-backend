package com.gogofnd.kb.domain.insurance.repository;

import com.gogofnd.kb.domain.insurance.entity.RejectReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectReasonRepository extends JpaRepository<RejectReason,Long> {
}
