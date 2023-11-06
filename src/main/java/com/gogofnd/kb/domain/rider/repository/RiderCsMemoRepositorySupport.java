package com.gogofnd.kb.domain.rider.repository;


import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.gogofnd.kb.domain.rider.entity.QRider.rider;

@RequiredArgsConstructor
@Repository
public class RiderCsMemoRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public Rider findByRiderLoginId(String loginId){
        Rider resultRider = queryFactory
                .selectFrom(rider)
                .where(rider.loginId.eq(loginId))
                .fetchOne();

        if(resultRider == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRider;
    }

}
