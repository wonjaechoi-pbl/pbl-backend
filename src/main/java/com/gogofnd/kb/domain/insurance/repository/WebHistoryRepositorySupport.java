package com.gogofnd.kb.domain.insurance.repository;

import com.gogofnd.kb.domain.insurance.entity.WebHistory;
import com.gogofnd.kb.domain.insurance.entity.WebRejectReason;
import com.gogofnd.kb.domain.rider.entity.RiderWeb;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.gogofnd.kb.domain.insurance.entity.QWebHistory.webHistory;
import static com.gogofnd.kb.domain.insurance.entity.QWebRejectReason.webRejectReason;
import static com.gogofnd.kb.domain.rider.entity.QRiderWeb.riderWeb;

@RequiredArgsConstructor
@Repository
public class WebHistoryRepositorySupport {
    private final JPAQueryFactory queryFactory;
    public int findTotalYoungRiderCount(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = LocalDateTime.of(now.getYear(),now.getMonthValue(),1,0,0,0);
        return (int) queryFactory.selectFrom(webHistory)
                .where(webHistory.ageYn.eq("Y"))
                .where(webHistory.createdDate.between(firstDay,now))
                .stream().count();

    }
    public int findTotalOlderRiderCount(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = LocalDateTime.now().minusDays(now.getDayOfMonth()+1);

        return (int) queryFactory.selectFrom(webHistory)
                .where(webHistory.ageYn.eq("N"))
                .where(webHistory.createdDate.between(firstDay,now))
                .stream().count();

    }

    public int findYoungRiderCount(String sellerCode){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = LocalDateTime.of(now.getYear(),now.getMonthValue(),1,0,0,0);
        return (int) queryFactory.selectFrom(webHistory)
                .where(webHistory.ageYn.eq("Y"))
                .where(webHistory.createdDate.between(firstDay,now))
                .where(webHistory.seller.sellerCode.eq(sellerCode))
                .stream().count();

    }

    public int findOlderRiderCount(String sellerCode){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = LocalDateTime.of(now.getYear(),now.getMonthValue(),1,0,0,0);
        return (int) queryFactory.selectFrom(webHistory)
                .where(webHistory.ageYn.eq("N"))
                .where(webHistory.createdDate.between(firstDay,now))
                .where(webHistory.seller.sellerCode.eq(sellerCode))
                .stream().count();

    }

    //상태 값 변경 어제일어남
    public List<WebHistory> findRequestsByInsuranceStatusYesterday(String insuranceStatus){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23,59,59));

        return queryFactory.selectFrom(webHistory)
                .where(webHistory.status.eq(insuranceStatus))
                        .where(webHistory.createdDate.between(startDate,endDate))
                .fetch();
    }

    // 상태 값 변경 오늘일어남
    public List<WebHistory> findRequestsByInsuranceStatusToday(String insuranceStatus){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));

        return queryFactory.selectFrom(webHistory)
                .where(webHistory.status.eq(insuranceStatus))
                .where(webHistory.createdDate.between(startDate,endDate))
                .fetch();
    }

    public List<RiderWeb> findKakaoBatch(String insuranceStatus){
        return queryFactory.selectFrom(riderWeb)
                .where(riderWeb.insuranceStatus.eq(insuranceStatus))
                .fetch();
    }

    public List<RiderWeb> findKakaoBatchByDriverId(String driverId){
        return queryFactory.selectFrom(riderWeb)
                .where(riderWeb.driver_id.eq(driverId))
                .fetch();
    }

    // 상태 값 변경 오늘일어남
    public List<WebHistory> findRequestsByInsuranceStatusTodayForAPi4(){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));

        return queryFactory.selectFrom(webHistory)
                .where(webHistory.status.eq("032"))
                .where(webHistory.createdDate.between(startDate,endDate))
                .fetch();
    }

    public WebRejectReason findByRiderWebAndStatusOrderCrd(Long riderWeb_Id, String status){
        return queryFactory
                .selectFrom(webRejectReason)
                .where(webRejectReason.riderWeb.id.eq(riderWeb_Id), webRejectReason.status.eq(status))
                .orderBy(webRejectReason.reject_date.desc())
                .limit(1)
                .fetchOne();
    }
}
