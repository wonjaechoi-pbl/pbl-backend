package com.gogofnd.kb.domain.delivery.repository;

import com.gogofnd.kb.domain.delivery.dto.accident.AccidentRes;
import com.gogofnd.kb.domain.delivery.entity.Accident;
import com.gogofnd.kb.domain.rider.entity.QRider;
import com.gogofnd.kb.domain.seller.entity.*;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


import static com.gogofnd.kb.domain.delivery.entity.QAccident.accident;
import static com.gogofnd.kb.domain.rider.entity.QRider.*;
import static com.gogofnd.kb.domain.seller.entity.QCall.*;
import static com.gogofnd.kb.domain.seller.entity.QSeller.*;

@RequiredArgsConstructor
@Repository
public class AccidentRepositorySupport {
    private final JPAQueryFactory queryFactory;


    //전화번호로 사고 테이블 전체조회
    public List<Accident> findAccidentsByPhone(String phone){
        return queryFactory.selectFrom(accident)
                .where(accident.call.rider.phone.eq(phone))
                .orderBy(accident.accident_time.desc())
                .fetch();
    }

    // 전화번호로 사고 테이블 카운트 조회
    public Integer findAccidentsCountByPhone(String phone){
        return queryFactory.selectFrom(accident)
                .innerJoin(call).on(accident.call.id.eq(call.id))
                .innerJoin(rider).on(call.rider.id.eq(rider.id)).fetchJoin()
                .where(rider.phone.eq(phone))
                .distinct()
                .fetch().size();
    }

    //운영사 전체 보상금 조회
    public Integer findTotalCompensation(String sellerCode){

        System.out.println(sellerCode);
        return queryFactory.select(accident.compensation.sum())
                .from(accident)
                .innerJoin(call)
                .on(accident.call.id.eq(call.id))
                .innerJoin(rider)
                .on(call.rider.id.eq(rider.id))
                .innerJoin(seller)
                .on(seller.sellerCode.eq(sellerCode))
                .where(seller.sellerCode.eq(sellerCode))
                .fetchOne();
    }
}
