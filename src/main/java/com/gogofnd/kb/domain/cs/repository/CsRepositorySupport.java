package com.gogofnd.kb.domain.cs.repository;


import com.gogofnd.kb.domain.cs.dto.req.*;
import com.gogofnd.kb.domain.cs.dto.res.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.gogofnd.kb.domain.insurance.entity.QRejectMessage.rejectMessage1;
import static com.gogofnd.kb.domain.insurance.entity.QRejectReason.rejectReason1;
import static com.gogofnd.kb.domain.rider.entity.QRider.rider;
import static com.gogofnd.kb.domain.seller.entity.QCall.call;
import static com.gogofnd.kb.domain.seller.entity.QCallSettlement.callSettlement;
import static com.gogofnd.kb.domain.seller.entity.QSeller.seller;
import static com.gogofnd.kb.domain.delivery.entity.QAccident.accident;
import static com.gogofnd.kb.domain.delivery.entity.QKbBalancesHistory.kbBalancesHistory;

@RequiredArgsConstructor
@Repository
@Slf4j
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
        if(!ObjectUtils.isEmpty(req.getStartDate()) && !ObjectUtils.isEmpty(req.getEndDate())) {
            builder.and(rider.createdDate.between(stringToDate(req.getStartDate()), stringToDate(req.getEndDate()).plusDays(1)));
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
                        builder
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
                                builder
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
        if(!ObjectUtils.isEmpty(req.getStartDateTime()) && !ObjectUtils.isEmpty(req.getEndDateTime())) {
            builder.and(call.callAppointTime.goe(stringToDateTime(req.getStartDateTime())));
            builder.and(call.callCompleteTime.loe(stringToDateTime(req.getEndDateTime())));
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
        if(!ObjectUtils.isEmpty(req.getStartDateTime()) && !ObjectUtils.isEmpty(req.getEndDateTime())) {
            builder.and(callSettlement.callPickUpTime.goe(stringToDateTime(req.getStartDateTime())));
            builder.and(callSettlement.callCompleteTime.loe(stringToDateTime(req.getEndDateTime())));
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
        if(!ObjectUtils.isEmpty(req.getAccidentTime())) {
            builder.and(accident.accident_time.between(stringToDate(req.getAccidentTime()),stringToDate(req.getAccidentTime()).plusDays(1)));
        }
        if(!ObjectUtils.isEmpty(req.getClaimTime())) {
            builder.and(accident.claim_time.between(stringToDate(req.getClaimTime()),stringToDate(req.getClaimTime()).plusDays(1)));
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

    // 운영사 List 조회
    public Page<SellerRes> selectSellerList(Pageable pageable, SellerReq req) {

        BooleanBuilder builder = new BooleanBuilder();

        if(!ObjectUtils.isEmpty(req.getSellerName())) {
            builder.and(seller.name.like("%" + req.getSellerName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getBossName())) {
            builder.and(seller.bossName.like("%" + req.getBossName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getInsureType())) {
            builder.and(seller.insureType.like("%" + req.getInsureType() + "%"));
        }

        List<SellerRes> result;
        result = queryFactory
                .select(Projections.fields(SellerRes.class,
                        seller.cmpcd.as("cmpcd"),
                        seller.name.as("sellerName"),
                        seller.bossName.as("bossName"),
                        seller.businessNumber.as("businessNumber"),
                        seller.address.as("address"),
                        seller.application_number.as("applicationNumber"),
                        seller.policy_number.as("policyNumber"),
                        seller.balance.as("balance"),
                        seller.insureType.as("InsureType")
                ))
                .from(seller)
                .where(
                        builder
                )
                .orderBy(seller.cmpcd.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalCount =
                queryFactory
                        .selectFrom(seller)
                        .where(
                                builder
                        )
                        .orderBy(seller.cmpcd.asc())
                        .fetch()
                        .size();
        return new PageImpl<>(result, pageable, totalCount);
    }

    // 보험사 예납금 이력 List 조회
    public Page<KbBalanceHistoryRes> selectKbBalanceHistoryList(Pageable pageable, KbBalanceHistoryReq req) {

        BooleanBuilder builder = new BooleanBuilder();

        if(!ObjectUtils.isEmpty(req.getSellerName())) {
            builder.and(seller.name.like("%" + req.getSellerName() + "%"));
        }
        if(!ObjectUtils.isEmpty(req.getDate())) {
            builder.and(kbBalancesHistory.date.between(stringToDateSec(req.getDate()),stringToDateSec(req.getDate())));
        }

        List<KbBalanceHistoryRes> result;
        result = queryFactory
                .select(Projections.fields(KbBalanceHistoryRes.class,
                        seller.name.as("sellerName"),
                        kbBalancesHistory.balance.as("balance"),
                        kbBalancesHistory.cmpcd.as("cmpcd"),
                        kbBalancesHistory.date.as("date"),
                        kbBalancesHistory.useAmt.as("useAmt")
                ))
                .from(kbBalancesHistory)
                .leftJoin(seller).on(kbBalancesHistory.cmpcd.eq(seller.cmpcd))
                .where(
                        builder
                )
                .orderBy(kbBalancesHistory.cmpcd.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalCount =
                queryFactory
                        .selectFrom(kbBalancesHistory)
                        .leftJoin(seller).on(kbBalancesHistory.cmpcd.eq(seller.cmpcd))
                        .where(
                                builder
                        )
                        .orderBy(kbBalancesHistory.cmpcd.asc())
                        .fetch()
                        .size();
        return new PageImpl<>(result, pageable, totalCount);
    }

    private LocalDateTime stringToDateTime(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return LocalDateTime.parse(dateTime, formatter);
    }

    private LocalDate stringToDateSec(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return LocalDate.parse(date, formatter);
    }

    private LocalDateTime stringToDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return LocalDate.parse(date, formatter).atStartOfDay();
    }
}
