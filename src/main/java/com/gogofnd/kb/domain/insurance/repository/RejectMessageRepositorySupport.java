package com.gogofnd.kb.domain.insurance.repository;


import com.gogofnd.kb.domain.insurance.entity.QRejectMessage;
import com.gogofnd.kb.domain.insurance.entity.RejectMessage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RejectMessageRepositorySupport {

    private final JPAQueryFactory queryFactory;



    public RejectMessage findRejectMessage(String rejectReason){
        return queryFactory.selectFrom(QRejectMessage.rejectMessage1)
                .where(QRejectMessage.rejectMessage1.rejectReason.eq(rejectReason))
                .fetchOne();
    }

}
