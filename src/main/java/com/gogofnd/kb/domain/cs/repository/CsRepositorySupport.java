package com.gogofnd.kb.domain.cs.repository;


import com.gogofnd.kb.domain.cs.dto.req.AccidentReq;
import com.gogofnd.kb.domain.cs.dto.req.CallsSettlementReq;
import com.gogofnd.kb.domain.cs.dto.req.InsureHistoryReq;
import com.gogofnd.kb.domain.cs.dto.req.RealTimeCallsReq;
import com.gogofnd.kb.domain.cs.dto.res.AccidentRes;
import com.gogofnd.kb.domain.cs.dto.res.CallsSettlementRes;
import com.gogofnd.kb.domain.cs.dto.res.InsureHistoryRes;
import com.gogofnd.kb.domain.cs.dto.res.RealTimeCallsRes;
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

import static com.gogofnd.kb.domain.insurance.entity.QRejectMessage.rejectMessage1;
import static com.gogofnd.kb.domain.insurance.entity.QRejectReason.rejectReason1;
import static com.gogofnd.kb.domain.rider.entity.QRider.rider;
import static com.gogofnd.kb.domain.seller.entity.QCall.call;
import static com.gogofnd.kb.domain.seller.entity.QCallSettlement.callSettlement;
import static com.gogofnd.kb.domain.seller.entity.QSeller.seller;
import static com.gogofnd.kb.domain.delivery.entity.QAccident.accident;

@RequiredArgsConstructor
@Repository
public class CsRepositorySupport {
    private final JPAQueryFactory queryFactory;

//     로그인 ID로 Rider 조회
//    public Rider findByRiderLoginId(String loginId){
//        Rider resultRider = queryFactory
//                .selectFrom(rider)
//                .where(rider.loginId.eq(loginId))
//                .fetchOne();
//
//        if(resultRider == null){
//            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
//        }
//        return resultRider;
//    }

    // 보험 가입 상태 List 조회
    public Page<InsureHistoryRes> selectInsureHistoryList(Pageable pageable, InsureHistoryReq req) {

        BooleanBuilder builder = new BooleanBuilder();

        if(!ObjectUtils.isEmpty(req.getName())) {
            builder.and(rider.name.like("%" + req.getName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getPhone())) {
            builder.and(rider.phone.like("%" + req.getPhone() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getInsureStatus())) {
            builder.and(rider.insuranceStatus.like("%" + req.getInsureStatus() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getBirthDate())) {
            builder.and(rider.birthDate.like("%" + req.getBirthDate() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getSellerName())) {
            builder.and(rider.seller.name.like("%" + req.getSellerName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getUseYn())) {
            builder.and(rider.useYn.like("%" + req.getUseYn() + "%"));
        }


        List<InsureHistoryRes> resultRiderCs = queryFactory
                .select(Projections.fields(InsureHistoryRes.class,
                        rider.id.as("id"),
                        rider.seller.cmpcd.as("cmpcd"),
                        rider.seller.name.as("sellerName"),
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
                        betweenDate(req)
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
                                betweenDate(req)
                                , builder
                        )
                        .groupBy(rider.id)
                        .orderBy(rider.createdDate.desc())
                        .fetch()
                        .size();
        return new PageImpl<>(resultRiderCs, pageable, totalCount);
    }

    // 실시간 운행 이력 List 조회
    public Page<RealTimeCallsRes> selectRealTimeCallsList(Pageable pageable, RealTimeCallsReq req) {

        BooleanBuilder builder = new BooleanBuilder();

        if(!ObjectUtils.isEmpty(req.getCallId())) {
            builder.and(call.callId.like("%" + req.getCallId() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getGroupId())) {
            builder.and(call.groupId.like("%" + req.getGroupId() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getName())) {
            builder.and(call.rider.name.like("%" + req.getName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getDeliveryStatus())) {
            builder.and(call.delivery_status.like("%" + req.getDeliveryStatus() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getSellerName())) {
            builder.and(call.rider.seller.name.like("%" + req.getSellerName() + "%"));
        }

        List<RealTimeCallsRes> result;
        result = queryFactory
                .select(Projections.fields(RealTimeCallsRes.class,
                        seller.name.as("sellerName"),
                        rider.name.as("name"),
                        call.callId.as("callId"),
                        call.groupId.as("groupId"),
                        call.callAppointTime.as("startDateTime"),
                        call.callCompleteTime.as("endDateTime"),
                        call.delivery_status.as("deliveryStatus"),
                        call.kb_call_id.as("kbCallId")
                ))
                .from(call)
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .leftJoin(seller).on(rider.seller.id.eq(seller.id))
                .where(
//                        betweenDate(req)
                        builder
                )
                .orderBy(call.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalCount =
                queryFactory
                        .selectFrom(call)
                        .leftJoin(rider).on(call.rider.id.eq(rider.id))
                        .leftJoin(seller).on(rider.seller.id.eq(seller.id))
                        .where(
//                                betweenDate(req)
                                builder
                        )
                        .orderBy(call.id.desc())
                        .fetch()
                        .size();
        return new PageImpl<>(result, pageable, totalCount);
    }

    // 정산 운행 이력 List 조회
    public Page<CallsSettlementRes> selectCallsSettlementList(Pageable pageable, CallsSettlementReq req) {

        BooleanBuilder builder = new BooleanBuilder();

        if(!ObjectUtils.isEmpty(req.getGroupId())) {
            builder.and(callSettlement.groupId.like("%" + req.getGroupId() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getName())) {
            builder.and(callSettlement.rider.name.like("%" + req.getName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getSellerName())) {
            builder.and(callSettlement.rider.seller.name.like("%" + req.getSellerName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getSettlementStatus())) {
            builder.and(callSettlement.settlementStatus.like("%" + req.getSettlementStatus() + "%"));
        }

        List<CallsSettlementRes> result;
        result = queryFactory
                .select(Projections.fields(CallsSettlementRes.class,
                        seller.name.as("sellerName"),
                        rider.name.as("name"),
                        callSettlement.groupId.as("groupId"),
                        callSettlement.callPickUpTime.as("startDateTime"),
                        callSettlement.callCompleteTime.as("endDateTime"),
                        callSettlement.settlementStatus.as("settlementStatus"),
                        callSettlement.balance.as("balance")
                ))
                .from(callSettlement)
                .leftJoin(rider).on(callSettlement.rider.id.eq(rider.id))
                .leftJoin(seller).on(rider.seller.id.eq(seller.id))
                .where(
//                        betweenDate(req)
                        builder
                )
                .orderBy(callSettlement.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalCount =
                queryFactory
                        .selectFrom(callSettlement)
                        .leftJoin(rider).on(callSettlement.rider.id.eq(rider.id))
                        .leftJoin(seller).on(rider.seller.id.eq(seller.id))
                        .where(
//                                betweenDate(req)
                                builder
                        )
                        .orderBy(callSettlement.id.desc())
                        .fetch()
                        .size();
        return new PageImpl<>(result, pageable, totalCount);
    }

    // 사고 이력 List 조회
    public Page<AccidentRes> selectAccidentList(Pageable pageable, AccidentReq req) {

        BooleanBuilder builder = new BooleanBuilder();

        if(!ObjectUtils.isEmpty(req.getName())) {
            builder.and(accident.call.rider.name.like("%" + req.getName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getSellerName())) {
            builder.and(accident.call.rider.seller.name.like("%" + req.getSellerName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getClaimNumber())) {
            builder.and(accident.claimNumber.like("%" + req.getClaimNumber() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getCallId())) {
            builder.and(accident.call.callId.like("%" + req.getCallId() + "%"));
        }

        List<AccidentRes> result;
        result = queryFactory
                .select(Projections.fields(AccidentRes.class,
                        rider.seller.name.as("sellerName"),
                        rider.name.as("name"),
                        accident.claimNumber.as("claimNumber"),
                        accident.claim_time.as("claimTime"),
                        accident.accident_time.as("accidentTime"),
                        call.callId.as("callId")
                ))
                .from(accident)
                .leftJoin(call).on(accident.call.id.eq(call.id))
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .where(
                        builder
                )
                .orderBy(accident.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalCount =
                queryFactory
                        .selectFrom(accident)
                        .leftJoin(call).on(accident.call.id.eq(call.id))
                        .leftJoin(rider).on(call.rider.id.eq(rider.id))
                        .where(
                                builder
                        )
                        .orderBy(accident.id.desc())
                        .fetch()
                        .size();
        return new PageImpl<>(result, pageable, totalCount);
    }

    private BooleanExpression betweenDate(InsureHistoryReq req){
        if(req.getStartDate() == null || req.getEndDate() == null) return null;

        return rider.createdDate.between(req.getStartDate(), req.getEndDate().plusDays(1));
    }
}
