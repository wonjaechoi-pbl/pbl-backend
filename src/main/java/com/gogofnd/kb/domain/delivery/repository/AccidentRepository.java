package com.gogofnd.kb.domain.delivery.repository;

import com.gogofnd.kb.domain.delivery.entity.Accident;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccidentRepository extends JpaRepository<Accident,Long> {

    
}
