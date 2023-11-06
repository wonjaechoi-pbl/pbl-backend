package com.gogofnd.kb.domain.rider.repository;

import com.gogofnd.kb.domain.rider.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface RiderRepository extends JpaRepository<Rider,Long> {
    Optional<Rider> findByPhone(String phone);
    Optional<Rider> findByIdAndUseYn(Long id, String useYn);
    @Query("select r from Rider r where r.driver_id = ?1")
    Optional<Rider> findByDriver_id(String driver_id);
    @Query("select count(*) from Rider r WHERE r.phone LIKE '%'||?1||'%' AND r.useYn = ?2")
    Long countRiderByPhoneAndUseYn(String phone, String useYn);
}
