package com.gogofnd.kb.domain.rider.repository;


import com.gogofnd.kb.domain.rider.dto.req.RiderCsReq;
import com.gogofnd.kb.domain.rider.dto.res.RiderCsRes;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

import static com.gogofnd.kb.domain.insurance.entity.QRejectMessage.rejectMessage1;
import static com.gogofnd.kb.domain.insurance.entity.QRejectReason.rejectReason1;
import static com.gogofnd.kb.domain.rider.entity.QRider.rider;

@RequiredArgsConstructor
@Repository
public class RiderCsMemoRepositorySupport {
    private final JPAQueryFactory queryFactory;

    // 로그인 ID로 Rider 조회
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

    public Page<RiderCsRes> findRiderCsList(Pageable pageable, RiderCsReq riderCsReq) {

        BooleanBuilder builder = new BooleanBuilder();

        if (!ObjectUtils.isEmpty(riderCsReq.getSearchField())) {
            if(Objects.equals(riderCsReq.getSearchField(), "phone")) {
                builder.and(rider.phone.like("%" + riderCsReq.getKeyword() + "%"));
            }
            if(Objects.equals(riderCsReq.getSearchField(), "name")) {
                builder.and(rider.name.like("%" + riderCsReq.getKeyword() + "%"));
            }
            if(Objects.equals(riderCsReq.getSearchField(), "useYn")) {
                builder.and(rider.useYn.like("%" + riderCsReq.getKeyword() + "%"));
            }
            if(Objects.equals(riderCsReq.getSearchField(), "birthDate")) {
                builder.and(rider.birthDate.like("%" + riderCsReq.getKeyword() + "%"));
            }
            if(Objects.equals(riderCsReq.getSearchField(), "insuranceStatus")) {
                builder.and(rider.insuranceStatus.like("%" + riderCsReq.getKeyword() + "%"));
            }
            if(Objects.equals(riderCsReq.getSearchField(), "sellerName")) {
                builder.and(rider.seller.name.like("%" + riderCsReq.getKeyword() + "%"));
            }
        }

        List<RiderCsRes> resultRiderCs = queryFactory
                .select(Projections.fields(RiderCsRes.class,
                        rider.id.as("id"),
                        rider.seller.cmpcd.as("cmpcd"),
                        rider.seller.name.as("group_name"),
                        rider.phone.as("phone"),
                        rider.name.as("name"),
                        rider.driver_id.as("driverId"),
                        rider.loginId.as("loginId"),
                        rider.birthDate.as("birthDate"),
                        rider.mtdt.as("mtdt"),
                        rider.createdDate.as("createdDate"),
                        rider.vcNumber.as("vcNumber"),
                        rider.insuranceStatus.as("insuranceStatus"),
                        rider.imagePath.as("imagePath"),
                        Expressions.cases()
                                .when(rider.insuranceStatus.eq("011")).then("가입설계동의 요청")
                                .when(rider.insuranceStatus.eq("021")).then("인수심사 진행 중")
                                .when(rider.insuranceStatus.eq("033")).then("인수심사 거절")
                                .when(rider.insuranceStatus.eq("034")).then("인수심사 보류")
                                .when(rider.insuranceStatus.eq("041")).then("계약체결동의 요청")
                                .when(rider.insuranceStatus.eq("051")).then("기명요청 진행 중")
                                .when(rider.insuranceStatus.eq("062")).then("기명요청 완료")
                                .when(rider.insuranceStatus.eq("063")).then("기명요청 거절")
                                .when(rider.insuranceStatus.eq("071")).then("기명취소 요청")
                                .when(rider.insuranceStatus.eq("082")).then("기명취소 완료")
                                .when(rider.insuranceStatus.eq("083")).then("기명취소 거절")
                                .otherwise(rejectMessage1.rejectMessage)
                                .as("status_name"),
                        rejectMessage1.rejectMessage.as("reject_message"),
                        rider.memo.as("memo"),
                        rider.memoWriter.as("memoWriter"),
                        rider.totalWebViewUrl.as("totalWebViewUrl")
                ))
                .from(rider)
                .join(rider.seller).on(rider.seller.id.eq(rider.seller.id))
                .leftJoin(rejectReason1).on(rejectReason1.rider.id.eq(rider.id))
                .leftJoin(rejectMessage1).on(rejectReason1.rejectReason.eq(rejectMessage1.rejectReason))
                .where(
                        betweenDate(riderCsReq)
                        , builder
                )
                .groupBy(rider.id)
                .orderBy(rider.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalCount =
                queryFactory
                        .selectFrom(rider)
                        .join(rider.seller).on(rider.seller.id.eq(rider.seller.id))
                        .leftJoin(rejectReason1).on(rejectReason1.rider.id.eq(rider.id))
                        .leftJoin(rejectMessage1).on(rejectReason1.rejectReason.eq(rejectMessage1.rejectReason))
                        .where(
                                betweenDate(riderCsReq)
                                , builder
                        )
                        .groupBy(rider.id)
                        .orderBy(rider.createdDate.desc())
                        .fetch()
                        .size();
        return new PageImpl<>(resultRiderCs, pageable, totalCount);
    }

    private BooleanExpression betweenDate(RiderCsReq riderCsReq){
        if(riderCsReq.getStartDate() == null || riderCsReq.getEndDate() == null) return null;

        return rider.createdDate.between(riderCsReq.getStartDate(), riderCsReq.getEndDate().plusDays(1));
    }
}
