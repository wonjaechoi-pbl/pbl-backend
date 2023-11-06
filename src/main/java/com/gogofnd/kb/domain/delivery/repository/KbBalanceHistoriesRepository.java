package com.gogofnd.kb.domain.delivery.repository;

import com.gogofnd.kb.domain.delivery.entity.KbBalancesHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface KbBalanceHistoriesRepository extends JpaRepository<KbBalancesHistory,Long> {

}
