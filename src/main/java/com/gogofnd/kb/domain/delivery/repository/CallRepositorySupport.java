package com.gogofnd.kb.domain.delivery.repository;

import com.gogofnd.kb.domain.delivery.dto.insure.res.DeliveryInsureAccidentResponseDto;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.business.dto.res.DailyDrivingRes;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.Size;

import static com.gogofnd.kb.domain.seller.entity.QCall.call;
import static com.gogofnd.kb.domain.rider.entity.QRider.rider;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CallRepositorySupport {
    private final JPAQueryFactory queryFactory;

    //운영사 콜 조회 + 날짜 검색
    public List<Call> findCallsBySellerCode(String sellerCode,LocalDateTime startDate,LocalDateTime endDate){
        return queryFactory.selectFrom(call)
                .where(call.rider.seller.sellerCode.eq(sellerCode),call.callAppointTime.between(startDate,endDate))
                .fetch();
    }


    public String callRider(String driver_id){

        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59)).minusDays(1);
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(00,00,00)).minusDays(1);

        List<Tuple> str1= queryFactory.select(call.rider.driver_id,
                        call.callPickUpTime,
                        call.callCompleteTime)
                .from(call)
                .where(call.callPickUpTime.between(startDate,endDate)
                        .and(call.callCompleteTime.isNotNull()))
                .fetch();
        System.out.println("queryFactory 1 " + str1.size() + " :: " + startDate + " :: " + endDate);


        long m_totalTime = 0;
        for(Tuple asd : str1 ) {
            if (asd.get(call.rider.driver_id).toString().equals(driver_id)) {
                m_totalTime += Duration.between(asd.get(call.callPickUpTime), asd.get(call.callCompleteTime)).getSeconds();
            }else{
                continue;
            }
        }

        String result = "";
        for(Tuple item : str1){
            if(item.get(call.rider.driver_id).equals(driver_id)){
                result = driver_id;
            }
        }

        System.out.println("queryFactory 2 : " + result);

        if(result == driver_id){

            return Integer.toString((int)Math.ceil((double) m_totalTime/60));
        }else {
            return "0";
        }
    }

    //오늘자 테스트 보낼때
    public String todayRider(String driver_id){
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(00,00,00));

        List<Tuple> str1= queryFactory.select(call.rider.driver_id,
                        call.callPickUpTime,
                        call.callCompleteTime)
                .from(call)
                .where(call.callPickUpTime.between(startDate,endDate)
                        .and(call.callCompleteTime.isNotNull()))
                .fetch();
        System.out.println("queryFactory 1 : " + str1.size() + " :: " + startDate + " :: " + endDate);


        int m_totalTime = 0;
        for(Tuple asd : str1 ) {
            if (asd.get(call.rider.driver_id).toString().equals(driver_id)) {
                int getMins = (int)Math.ceil((double) Duration.between(asd.get(call.callPickUpTime), asd.get(call.callCompleteTime)).getSeconds()/60);
                System.out.println("queryFactory 3  " + getMins);
                m_totalTime += getMins;
            }else{
                continue;
            }
        }

        String result = "";
        for(Tuple item : str1){
            if(item.get(call.rider.driver_id).equals(driver_id)){
                result = driver_id;
            }
        }
        if(result == driver_id){
            System.out.println(m_totalTime);
            return Integer.toString(m_totalTime);
        }else {
            return "0";
        }
    }


    //선택적 테스트 보낼때
    public String specificDateRider(LocalDate localDate,String driver_id){
        LocalDateTime endDate = LocalDateTime.of(localDate, LocalTime.of(23,59,59));
        LocalDateTime startDate = LocalDateTime.of(localDate, LocalTime.of(00,00,00));

        List<Tuple> str1= queryFactory.select(call.rider.driver_id,
                        call.callPickUpTime,
                        call.callCompleteTime)
                .from(call)
                .where(
                        call.callPickUpTime.between(startDate,endDate),
                        call.callCompleteTime.isNotNull()
                )
                .fetch();
        System.out.println("queryFactory 1 : " + str1.size() + " :: " + startDate + " :: " + endDate);


        int m_totalTime = 0;
        for(Tuple asd : str1 ) {
            if (asd.get(call.rider.driver_id).toString().equals(driver_id)) {
                int getMins = (int)Math.ceil((double) Duration.between(asd.get(call.callPickUpTime), asd.get(call.callCompleteTime)).getSeconds()/60);
                System.out.println("queryFactory 3  " + getMins);
                m_totalTime += getMins;
            }else{
                continue;
            }
        }
        String result = "";
        for(Tuple item : str1){
            if(item.get(call.rider.driver_id).equals(driver_id)){
                result = driver_id;
            }
        }
        if(result == driver_id){
            System.out.println(m_totalTime);
            return Integer.toString(m_totalTime);
        }else {
            return "0";
        }
    }



    //오늘자 테스트 보낼때
    public String specificDateRider(String driver_id,LocalDateTime localDateTime){
        LocalDateTime endDate = LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(23,59,59));
        LocalDateTime startDate = LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(00,00,00));

        System.out.println("localDateTime.toLocalDate() " +localDateTime.toLocalDate());

        List<Tuple> str1= queryFactory.select(call.rider.driver_id,
                        call.callPickUpTime,
                        call.callCompleteTime)
                .from(call)
                .where(call.callPickUpTime.between(startDate,endDate)
                        .and(call.callCompleteTime.isNotNull()))
                .fetch();
        System.out.println("queryFactory 1 : " + str1.size() + " :: " + startDate + " :: " + endDate);


        int m_totalTime = 0;
        for(Tuple asd : str1 ) {
            if (asd.get(call.rider.driver_id).toString().equals(driver_id)) {
                int getMins = (int)Math.ceil((double) Duration.between(asd.get(call.callPickUpTime), asd.get(call.callCompleteTime)).getSeconds()/60);
                System.out.println("queryFactory 3  " + getMins);
                m_totalTime += getMins;
            }else{
                continue;
            }
        }

        String result = "";
        for(Tuple item : str1){
            if(item.get(call.rider.driver_id).equals(driver_id)){
                result = driver_id;
            }
        }
        if(result == driver_id){
            System.out.println(m_totalTime);
            return Integer.toString(m_totalTime);
        }else {
            return "0";
        }
    }


    public Call findByFirstByOrder(String driverId){
        return queryFactory.selectFrom(call)
                .where(call.rider.driver_id.eq(driverId))
                .orderBy(call.id.desc())
                .fetchOne();
    }


    public Call findByCallId(String callId){
        Call resultCall = queryFactory
                .selectFrom(call)
                .where(call.callId.eq(callId))
                .fetchOne();
        if(resultCall == null){
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return resultCall;
    }

    public Call findByCallId2(String callId){
        Call resultCall = queryFactory
                .selectFrom(call)
                .where(call.callId.eq(callId))
                .fetchOne();
        return resultCall;
    }

    //배차시간으로 운행 조회
    public List<DeliveryInsureAccidentResponseDto> findCallsByAppointTime(Call callReq){
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));
        return queryFactory.select(Projections.constructor(DeliveryInsureAccidentResponseDto.class, call))
                .from(call)
                .where(call.callAppointTime.between(callReq.getCallAppointTime(), endDate),call.rider.id.eq(callReq.getRider().getId()))
                .fetch();
    }

    //당일 운행 조회
    public List<DailyDrivingRes> findDailyCalls(Long riderId){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(),LocalTime.MIN);

        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return queryFactory.select(Projections.constructor(DailyDrivingRes.class, call))
                .from(call)
                .where(call.callAppointTime.between(startDate, endDate)
                        ,call.rider.id.eq(riderId),
                        call.callCompleteTime.isNotNull())
                .fetch();
    }

    // 완료되지 않은 요청이 있을 경우. (밍) 에러코드에 call_id도 같이 보내준다. 나중에 배달사에서 call_id 못찾는 경우를 대비해서
    public long findByComplete(String loginId){
        System.out.println("LoginId :: " + loginId);
        long totalTime =0;


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        List<Tuple> resultcompleteList = queryFactory
                .select(call.callCompleteTime, rider.driver_id, call.callId,call.delivery_status, call.callPickUpTime)
                .from(call)
                .leftJoin(call.rider, rider)
                .where(
                        rider.loginId.eq(loginId),
                        rider.useYn.eq("Y")
                )
                .fetch();

        String empCompleteTime = "";
        for(Tuple item : resultcompleteList ){
            //System.out.println(item); // 해당 라이더의 콜 완료시간이 없을때, 동작

//                System.out.println("compareTo :: " + item.get(call.callPickUpTime).toLocalDate().compareTo(LocalDate.now()));

            // 완료되지 않은 시간제보험이 있습니다. - 비활성화 처리
//                if(item.get(call.callPickUpTime) == null || item.get(call.callCompleteTime) == null )
//                    throw new BusinessException(ErrorCode.COMPLETE_REJECTED,empCompleteTime);

//                System.out.println("compareTo :: " + item.get(call.callPickUpTime).toLocalDate().compareTo(LocalDate.now()));

            if(item.get(call.callPickUpTime).toLocalDate().compareTo(LocalDate.now()) == 0){
                Duration between = Duration.between(item.get(call.callPickUpTime), item.get(call.callCompleteTime));
                totalTime += Math.ceil((double) between.toSeconds()/60);

                System.out.println("pickupTime :: " + item.get(call.callPickUpTime) + " complete :: " + item.get(call.callCompleteTime) + " between Minute:: " + between.toMinutes() + " between totalTime :: " + totalTime);
            }
        }

        return totalTime;
    }

//    // 20230324 이성탄 일보험으로 바뀌는 경우 테스트
//    public int findBytotalBalance(String loginId) {
//        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(),LocalTime.MIN);
//        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
//
//        List<Long> totalbalList = queryFactory
//                .select(call.balance.sum())
//                .from(call)
//                .leftJoin(call.rider, rider)
//                .where(call.callCompleteTime.between(startDate, endDate), rider.loginId.eq(loginId))
//                .fetch();
//        return findBytotalBalance(loginId);
//    }

    public Call findByKbCallId(String callId){
        Call resultCall = queryFactory
                .selectFrom(call)
                .where(call.kb_call_id.eq(callId))
                .fetchOne();
        if(resultCall == null){
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return resultCall;
    }

    public int selectFirstRiderCall(String driverId) {

        return queryFactory
                .selectFrom(call)
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .where(rider.loginId.eq(driverId))
                .fetch()
                .size();
    }

    public List<String> findNullCompleteTimeCallId(String driverId) {

        return queryFactory
                .select(call.callId)
                .from(call)
                .leftJoin(rider).on(call.rider.id.eq(rider.id))
                .where(call.callCompleteTime.isNull())
                .where(rider.loginId.eq(driverId))
                .fetch();
    }

    public void updateNullCompleteTime(String callId, LocalDateTime endTime) {
        queryFactory
                .update(call)
                .set(call.callCompleteTime,endTime)
                .set(call.delivery_status,"완료")
                .where(call.callId.eq(callId))
                .execute();
    }
}