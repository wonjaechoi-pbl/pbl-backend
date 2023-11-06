package com.gogofnd.kb.domain.delivery.repository;


import com.gogofnd.kb.domain.seller.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CallRepository extends JpaRepository<Call,Long> {

    List<Call> findAllByCallRequestTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Call> findAllByCallRequestTimeBetweenAndRiderId(LocalDateTime start, LocalDateTime end, Long driver_id);



    List<Call> findAllByCallPickUpTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Call> findAllByCallPickUpTimeBetweenAndRiderId(LocalDateTime start, LocalDateTime end, Long driver_id);
}
