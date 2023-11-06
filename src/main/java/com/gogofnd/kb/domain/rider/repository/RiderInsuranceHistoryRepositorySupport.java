package com.gogofnd.kb.domain.rider.repository;


import com.gogofnd.kb.domain.rider.entity.RiderInsuranceHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.beans.Expression;
import java.time.LocalDateTime;

import static com.gogofnd.kb.domain.rider.entity.QRiderInsuranceHistory.*;

@RequiredArgsConstructor
@Repository
public class RiderInsuranceHistoryRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public RiderInsuranceHistory findByRiderPhone(String phone){
        return queryFactory.selectFrom(riderInsuranceHistory)
                .where(riderInsuranceHistory.rider.phone.eq(phone))
                .fetchOne();
    }

}
