package com.gogofnd.kb.domain.delivery.repository;

import com.gogofnd.kb.domain.seller.entity.CallSettlement;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.gogofnd.kb.domain.rider.entity.QRider.rider;
import static com.gogofnd.kb.domain.seller.entity.QCall.call;
import static com.gogofnd.kb.domain.seller.entity.QCallSettlement.callSettlement;

@Repository
@RequiredArgsConstructor
public class CallSettlementRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public int selectNullCompleteTimeCallCount(String driverId) {

        return queryFactory
                .selectFrom(call)
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .where(rider.loginId.eq(driverId))
                .where(call.callCompleteTime.isNull())
                .fetch()
                .size();
    }

    public String findCallsByLastGroupId(String riderId, String nowDate) {

        String prefix = "GR";
        StringExpression concatenatedGroupId = call.groupId.stringValue().concat(prefix + nowDate + "-"  + riderId +  "-");
        Predicate predicate = concatenatedGroupId.like(prefix + nowDate + "-"  + riderId + "-%");

        return queryFactory
                .select(call.groupId.max().stringValue())
                .from(call)
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .where(predicate)
                .fetchOne();
    }

    public String findCallsByDupeGroupId(String driverId) {

        return queryFactory
                .select(call.groupId.max().stringValue())
                .from(call)
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .where(rider.loginId.eq(driverId))
                .where(call.callCompleteTime.isNull())
                .fetchOne();
    }

    public int selectNewDateCallCount(String driverId) {

        LocalDate now = LocalDate.now();
        LocalDateTime formatterNowTime = now.atStartOfDay();

        return queryFactory
                .selectFrom(call)
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .where(rider.loginId.eq(driverId))
                .where(call.callAppointTime.goe(formatterNowTime))
                .fetch()
                .size();
    }

    public long findByComplete(String driverId){

        long totalTime = 0;

        List<Tuple> resultcompleteList = queryFactory
                .select(callSettlement.callCompleteTime, rider.driver_id, callSettlement.callPickUpTime)
                .from(callSettlement)
                .leftJoin(callSettlement.rider, rider)
                .where(
                        rider.loginId.eq(driverId),
                        rider.useYn.eq("Y"),
                        callSettlement.callCompleteTime.isNotNull()
                )
                .fetch();

        for(Tuple item : resultcompleteList ){
            if(item.get(callSettlement.callPickUpTime).toLocalDate().compareTo(LocalDate.now()) == 0){
                Duration between = Duration.between(item.get(callSettlement.callPickUpTime), item.get(callSettlement.callCompleteTime));
                totalTime += Math.ceil((double) between.toSeconds()/60);
            }
        }

        return totalTime;
    }

    public String findByGroupId(String callId) {

        return queryFactory
                .select(call.groupId.stringValue())
                .from(call)
                .where(call.callId.eq(callId))
                .fetchOne();
    }

    public CallSettlement findByCallSettlement(String groupId){

        CallSettlement result = queryFactory
                .selectFrom(callSettlement)
                .where(callSettlement.groupId.eq(groupId))
                .fetchOne();

        if(result == null){
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        return result;
    }

    public int findCallsByPickupStatusCount(String driverId, String groupId) {

        return queryFactory
                .selectFrom(call)
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .where(rider.loginId.eq(driverId))
                .where(call.groupId.eq(groupId))
                .where(call.callCompleteTime.isNull())
                .fetch()
                .size();
    }

    public List<String> findCallsByGroupingCallId(String groupId) {

        return queryFactory
                .select(call.callId.stringValue())
                .from(call)
                .where(call.groupId.eq(groupId))
                .fetch();
    }

    public String getDaysRiderTotalTime(String driver_id, LocalDateTime startDate, LocalDateTime endDate){

        List<Tuple> str1= queryFactory.select(callSettlement.rider.driver_id,
                        callSettlement.callPickUpTime,
                        callSettlement.callCompleteTime)
                .from(callSettlement)
                .where(
                        callSettlement.callCompleteTime.goe(startDate),
                        callSettlement.callCompleteTime.lt(endDate)
                )
                .fetch();
        System.out.println("queryFactory 1 " + str1.size() + " :: " + startDate + " :: " + endDate);

        long m_totalTime = 0;
        for(Tuple asd : str1 ) {
            if (asd.get(callSettlement.rider.driver_id).toString().equals(driver_id)) {
                m_totalTime += Duration.between(asd.get(callSettlement.callPickUpTime), asd.get(callSettlement.callCompleteTime)).getSeconds();
            }
        }

        String result = "";
        for(Tuple item : str1){
            if(item.get(callSettlement.rider.driver_id).equals(driver_id)){
                result = driver_id;
            }
        }

        System.out.println("queryFactory 2 : " + result);

        if(result.equals(driver_id)){
            return Integer.toString((int)Math.ceil((double) m_totalTime/60));
        }else {
            return "0";
        }
    }

    // 온나 Exception 관련
    public CallSettlement findCalls(String groupId) {

        return queryFactory
                .selectFrom(callSettlement)
                .where(callSettlement.groupId.eq(groupId))
                .fetchOne();
    }

    public List<CallSettlement> findCallSettlementList(LocalDateTime startDatetime, LocalDateTime endDatetime) {

        return  queryFactory
                .selectFrom(callSettlement)
                .leftJoin(rider).on(callSettlement.rider.id.eq(rider.id))
                .where(
                        callSettlement.callCompleteTime.goe(startDatetime),
                        callSettlement.callCompleteTime.lt(endDatetime),
                        callSettlement.callCompleteTime.isNotNull(),
                        rider.seller.cmpcd.notIn("G02", "G05"),
                        rider.useYn.eq("Y")
                )
                .fetch();
    }

    public List<CallSettlement> findVrCallSettlementList(LocalDateTime startDatetime, LocalDateTime endDatetime) {

        return  queryFactory
                .selectFrom(callSettlement)
                .leftJoin(rider).on(callSettlement.rider.id.eq(rider.id))
                .where(
                        callSettlement.callCompleteTime.goe(startDatetime),
                        callSettlement.callCompleteTime.lt(endDatetime),
                        callSettlement.callCompleteTime.isNotNull(),
                        rider.seller.cmpcd.in("G02", "G05"),
                        rider.useYn.eq("Y")
                )
                .fetch();
    }

    public List<CallSettlement> findSpecificCallSettlementList(LocalDate localDate) {

        LocalDateTime startDatetime = LocalDateTime.of(localDate, LocalTime.of(0,0,0)).minusDays(1);
        LocalDateTime endDatetime = LocalDateTime.of(localDate, LocalTime.of(0,0,0));

        return  queryFactory
                .selectFrom(callSettlement)
                .leftJoin(rider).on(callSettlement.rider.id.eq(rider.id))
                .where(
                        callSettlement.callCompleteTime.goe(startDatetime),
                        callSettlement.callCompleteTime.lt(endDatetime),
                        callSettlement.callCompleteTime.isNotNull(),
                        rider.seller.cmpcd.notIn("G02", "G05"),
                        rider.useYn.eq("Y")
                )
                .fetch();
    }

    public List<CallSettlement> findSpecificVrCallSettlementList(LocalDate localDate) {

        LocalDateTime startDatetime = LocalDateTime.of(localDate, LocalTime.of(6,0,0)).minusDays(1);
        LocalDateTime endDatetime = LocalDateTime.of(localDate,LocalTime.of(6,0,0));

        return  queryFactory
                .selectFrom(callSettlement)
                .leftJoin(rider).on(callSettlement.rider.id.eq(rider.id))
                .where(
                        callSettlement.callCompleteTime.goe(startDatetime),
                        callSettlement.callCompleteTime.lt(endDatetime),
                        callSettlement.callCompleteTime.isNotNull(),
                        rider.seller.cmpcd.in("G02", "G05"),
                        rider.useYn.eq("Y")
                )
                .fetch();
    }

    public void updateSettlementStatus(String groupId, String status) {

        queryFactory
                .update(callSettlement)
                .set(callSettlement.settlementStatus, status)
                .where(callSettlement.groupId.eq(groupId))
                .execute();
    }
}
