package com.gogofnd.kb.domain.rider.repository;

import com.gogofnd.kb.domain.rider.entity.RiderWeb;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RiderWebRepository extends JpaRepository<RiderWeb,Long> {
    Optional<RiderWeb> findByPhone(String phone);
}
