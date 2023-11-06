package com.gogofnd.kb.domain.rider.repository;


import com.gogofnd.kb.domain.rider.entity.RiderWebInsuranceHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.gogofnd.kb.domain.rider.entity.QRiderWebInsuranceHistory.riderWebInsuranceHistory;

@RequiredArgsConstructor
@Repository
public class RiderWebInsuranceHistoryRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public RiderWebInsuranceHistory findByRiderWebPhone(String phone){
        return queryFactory.selectFrom(riderWebInsuranceHistory)
                .where(riderWebInsuranceHistory.riderWeb.phone.eq(phone))
                .fetchOne();
    }

}
