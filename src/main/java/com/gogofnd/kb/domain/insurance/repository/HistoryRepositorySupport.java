package com.gogofnd.kb.domain.insurance.repository;

import com.gogofnd.kb.business.dto.res.InsuranceStatusListRes;
import com.gogofnd.kb.domain.insurance.entity.History;
import com.gogofnd.kb.domain.insurance.entity.RejectReason;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.gogofnd.kb.domain.insurance.entity.QHistory.history;
import static com.gogofnd.kb.domain.insurance.entity.QRejectReason.rejectReason1;
import static com.gogofnd.kb.domain.rider.entity.QRider.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class HistoryRepositorySupport {
    private final JPAQueryFactory queryFactory;
    public int findTotalYoungRiderCount(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = LocalDateTime.of(now.getYear(),now.getMonthValue(),1,0,0,0);
        return (int) queryFactory.selectFrom(history)
                .where(history.ageYn.eq("Y"))
                .where(history.createdDate.between(firstDay,now))
                .stream().count();

    }
    public int findTotalOlderRiderCount(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = LocalDateTime.now().minusDays(now.getDayOfMonth()+1);

        return (int) queryFactory.selectFrom(history)
                .where(history.ageYn.eq("N"))
                .where(history.createdDate.between(firstDay,now))
                .stream().count();

    }

    public int findYoungRiderCount(String sellerCode){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = LocalDateTime.of(now.getYear(),now.getMonthValue(),1,0,0,0);
        return (int) queryFactory.selectFrom(history)
                .where(history.ageYn.eq("Y"))
                .where(history.createdDate.between(firstDay,now))
                .where(history.seller.sellerCode.eq(sellerCode))
                .stream().count();

    }

    public int findOlderRiderCount(String sellerCode){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = LocalDateTime.of(now.getYear(),now.getMonthValue(),1,0,0,0);
        return (int) queryFactory.selectFrom(history)
                .where(history.ageYn.eq("N"))
                .where(history.createdDate.between(firstDay,now))
                .where(history.seller.sellerCode.eq(sellerCode))
                .stream().count();

    }

    //상태 값 변경 어제일어남
    public List<History> findRequestsByInsuranceStatusYesterday(String insuranceStatus){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23,59,59));

        return queryFactory.selectFrom(history)
                .leftJoin(rider).on(history.rider.id.eq(rider.id))
                .where(rider.insuranceStatus.ne("062"))
                .where(history.status.eq(insuranceStatus))
                .where(history.createdDate.between(startDate,endDate))
                .where(history.rider.useYn.eq("Y"))
                .groupBy(history.rider.id)
                .fetch();
    }

    // 상태 값 변경 오늘일어남
    public List<History> findRequestsByInsuranceStatusToday(String insuranceStatus){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));

        return queryFactory.selectFrom(history)
                .where(history.status.eq(insuranceStatus))
                .where(history.createdDate.between(startDate,endDate))
                .fetch();
    }

    // 해당 라이더의 상태 값 변경 오늘일어남
    public int findRequestsByRiderInsuranceStatusToday(String insuranceStatus,Long id){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));

        return queryFactory.selectFrom(history)
                .where(history.status.eq(insuranceStatus))
                .where(history.createdDate.between(startDate,endDate))
                .where(history.rider.id.eq(id))
                .fetch().size();
    }

    public List<Rider> findKakaoBatch(String insuranceStatus){
        return queryFactory.selectFrom(rider)
                .where(rider.insuranceStatus.eq(insuranceStatus))
                .fetch();
    }

    public Rider findKakaoBatchByDriverId(String driverId){
        return queryFactory.selectFrom(rider)
                .where(rider.driver_id.eq(driverId))
                .fetchOne();
    }

    // 상태 값 변경 오늘일어남
    public List<History> findRequestsByInsuranceStatusTodayForAPi4(){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));

        return queryFactory.selectFrom(history)
                .where(history.status.eq("032"))
                .where(history.createdDate.between(startDate,endDate))
                .fetch();
    }

    public History findByInsuranceStatusAPi4Specific(Long riderId){
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));

        return queryFactory.selectFrom(history)
//                .where(history.status.eq("032"))
                .where(history.createdDate.between(startDate,endDate))
                .where(history.rider.id.eq(riderId))
                .fetchOne();
    }

    public RejectReason findByRiderAndStatusOrderCrd(Long riderId,String status){
        return queryFactory
                .selectFrom(rejectReason1)
                .where(rejectReason1.rider.id.eq(riderId),rejectReason1.status.eq(status))
                .orderBy(rejectReason1.reject_date.desc())
                .limit(1)
                .fetchOne();
    }

    public List<InsuranceStatusListRes> findByInsuranceStatus() {
        return queryFactory.select(
                Projections.constructor(
                        InsuranceStatusListRes.class,
                        rider.id, rider.name, rider.vcNumber,
                        rider.driver_id, rider.region, rider.loginId,
                        rider.insuranceStatus, rider.mtdt, rider.oprn_purp,
                        rider.createdDate, rider.modifiedDate,
                        rejectReason1.rejectReason
                )
            ).from(rider)
            .innerJoin(rejectReason1)
            .on(rejectReason1.rider.id.eq(rider.id)).fetchJoin()
            .groupBy(rider.name)
            .orderBy(rider.createdDate.desc())
            .fetch();
    }

    public int findHistoryApplicable(Long riderId, String status) {
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23,59,59));

        return queryFactory
                .selectFrom(history)
                .where(history.createdDate.between(startDate,endDate))
                .where(history.status.eq(status))
                .where(history.rider.id.eq(riderId))
                .fetch()
                .size();
    }
}
