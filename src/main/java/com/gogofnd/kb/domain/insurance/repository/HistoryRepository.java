package com.gogofnd.kb.domain.insurance.repository;

import com.gogofnd.kb.domain.insurance.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History,Long> {
}
