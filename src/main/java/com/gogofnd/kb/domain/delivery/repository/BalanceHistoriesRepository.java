package com.gogofnd.kb.domain.delivery.repository;

import com.gogofnd.kb.domain.delivery.entity.BalanceHistory;
import com.gogofnd.kb.domain.seller.entity.Call;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface BalanceHistoriesRepository extends JpaRepository<BalanceHistory,Long> {
}
