package com.gogofnd.kb.domain.rider.repository;

import com.gogofnd.kb.business.dto.req.*;
import com.gogofnd.kb.business.dto.res.CallsResultRes;
import com.gogofnd.kb.domain.rider.dto.req.RiderCsReq;
import com.gogofnd.kb.domain.rider.dto.res.RiderCsRes;
import com.gogofnd.kb.domain.rider.dto.res.RiderGogoraRes;
import com.gogofnd.kb.domain.rider.dto.res.RiderIdSsnRes;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.domain.seller.entity.Seller;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.AES_Encryption;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static com.gogofnd.kb.domain.insurance.entity.QRejectMessage.rejectMessage1;
import static com.gogofnd.kb.domain.insurance.entity.QRejectReason.rejectReason1;
import static com.gogofnd.kb.domain.rider.entity.QRider.rider;
import static com.gogofnd.kb.domain.seller.entity.QSeller.seller;
import static com.gogofnd.kb.domain.seller.entity.QCall.call;
import static com.gogofnd.kb.domain.insurance.entity.QHistory.history;

@RequiredArgsConstructor
@Repository
public class RiderRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public Rider findById(Long id){
        Rider resultRider = queryFactory
                .selectFrom(rider)
                .where(rider.id.eq(id))
                .fetchOne();

        if(resultRider == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRider;
    }

    public Rider findByDriverId(String id){
        Rider resultRider = queryFactory
                .selectFrom(rider)
                .where(rider.driver_id.eq(id))
                .fetchOne();

        if(resultRider == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRider;
    }

    public Seller findBySellerId(Seller seller){
        Seller resultSeller = queryFactory
                .selectFrom(rider.seller)
                .where(rider.seller.id.eq(seller.getId()))
                .fetchOne();

        if(resultSeller == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultSeller;
    }

    public Rider findByLoginId(String id){
        System.out.println("Rider :: " + id);
        Rider resultRider = queryFactory
                .selectFrom(rider)
                .where(
                        rider.loginId.eq(id),
                        rider.useYn.eq("Y")
                )
                .fetchOne();

        if(resultRider == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRider;
    }

    public Rider findByDriverIdAndSellerCode(String driverId,String sellerCode){
        Rider resultRider = queryFactory
                .selectFrom(rider)
                .where(rider.driver_id.eq(driverId),rider.seller.sellerCode.eq(sellerCode))
                .fetchOne();
        if(resultRider == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRider;
    }

    public Rider findByLoginIdAndSellerCode(String driverId,String sellerCode){

        Rider resultRider = queryFactory
                .selectFrom(rider)
                .where(
                        rider.loginId.eq(driverId),
                        rider.seller.sellerCode.eq(sellerCode),
                        rider.useYn.eq("Y")
                )
                .fetchOne();

        if(resultRider != null){
            System.out.println("resultRider :: " + resultRider.getLoginId());
        }else{
            throw new BusinessException(ErrorCode.NOT_FOUND_USER); // 존재하지 않는 회원입니다.
        }

        return resultRider;
    }

    public Rider findByPhone(String phone){
        Rider result = queryFactory.selectFrom(rider)
                .where(rider.phone.eq(phone))
                .fetchOne();

        return result;
    }

    public List<Rider> findAllById(List<Long> id){

        List<Rider> resultRider = queryFactory
                .selectFrom(rider)
                .where(rider.id.in(id))
                .fetch();
        return resultRider;
    }


    public List<Rider> findAllByDriverId(List<String> driver_id) {

        List<Rider> resultRider = queryFactory
                .selectFrom(rider)
                .where(rider.driver_id.in(driver_id))
                .groupBy(rider.driver_id)
                .fetch();
        return resultRider;
    }

    public Rider findByRiderSsn(String ssn,String name){
        List<Rider> resultRider = queryFactory
                .selectFrom(rider)
                .where(rider.name.eq(name))
                .groupBy(rider.name)
                .fetch();

        for(Rider rider : resultRider){
            try {
                String decode_ssn =  aesDecode(rider.getSsn());
                System.out.println("decode_ssn" + decode_ssn);
                if(decode_ssn.equals(ssn)){
                    return rider;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if(resultRider == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRider.get(0);
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

    public void updateCsMemo(UpdateCsMemoReq param) {
        queryFactory
                .update(rider)
                .set(rider.memo, param.getMemo())
                .set(rider.memoWriter, param.getMemoWriter())
                .where(rider.id.eq(param.getId()))
                .execute();
    }

    public void updateRiderLoginId(UpdateLoginIdReq updateLoginIdReq) {
        queryFactory
                .update(rider)
                .set(rider.loginId,updateLoginIdReq.getLoginId())
                .where(rider.driver_id.eq(updateLoginIdReq.getDriverId()))
                .execute();
    }

    public Long updateVroongBalance(AddBalanceReq req) {

        Long balance = queryFactory
                                        .select(seller.balance)
                                        .from(seller)
                                        .where(seller.cmpcd.eq("G02"))
                                        .fetchOne();

        Long result = balance + req.getBalance();

        queryFactory
                .update(seller)
                .set(seller.balance,result)
                .where(seller.cmpcd.eq("G02"))
                .execute();

        return result;
    }

    public void updateRiderStatus(UpdateStatusReq req) {
        queryFactory
                .update(rider)
                .set(rider.payStatus,req.getStatus())
                .where(rider.driver_id.eq(req.getDriverId()))
                .execute();
    }
        
    public List<RiderCsRes> findAllRiderCsList(RiderCsReq riderCsReq) {

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

        if (!ObjectUtils.isEmpty(riderCsReq.getCmpcd())) {
            builder.and(rider.seller.cmpcd.eq(riderCsReq.getCmpcd()));
        }


        List<RiderCsRes> resultRiderCs = queryFactory
                .select(Projections.fields(RiderCsRes.class,
                        rider.seller.name.as("group_name"),
                        rider.loginId.as("loginId"),
                        rider.phone.as("phone"),
                        rider.name.as("name"),
                        rider.birthDate.as("birthDate"),
                        rider.mtdt.as("mtdt"),
                        rider.createdDate.as("createdDate"),
                        rider.insuranceStatus.as("insuranceStatus"),
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
                        rider.useYn.as("useYn")
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
                .fetch();

        return resultRiderCs;
    }

    public List<RiderGogoraRes> findRiderGoGoraInsuranceList(String cmpcd) {

        return queryFactory
                .select(Projections.fields(RiderGogoraRes.class,
                        rider.driver_id.as("driverId"),
                        rider.createdDate.as("createdDate"),
                        rider.insuranceStatus.as("insuranceStatus"),
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
                        rejectMessage1.rejectMessage.as("reject_message")
                ))
                .from(rider)
                .join(rider.seller).on(rider.seller.id.eq(rider.seller.id))
                .leftJoin(rejectReason1).on(rejectReason1.rider.id.eq(rider.id))
                .leftJoin(rejectMessage1).on(rejectReason1.rejectReason.eq(rejectMessage1.rejectReason))
                .where(
                        rider.useYn.eq("Y"),
                        rider.seller.cmpcd.eq(cmpcd)
                )
                .groupBy(rider.id)
                .orderBy(rider.createdDate.desc())
                .fetch();
    }

    public void updateInsureStartEndDate(UpdateInsureDateReq req) {
        queryFactory
                .update(rider)
                .set(rider.effectiveStartDate,req.getEffectiveStartDate())
                .set(rider.effectiveEndDate,req.getEffectiveEndDate())
                .set(rider.payStatus,"Y")
                .where(rider.driver_id.eq(req.getDriverId()))
                .execute();
    }

    public int findRiderInsureCount(String loginId) {

        return queryFactory
                .selectFrom(rider)
                .where(
                        rider.loginId.eq(loginId),
                        rider.insuranceStatus.eq("062"),
                        rider.useYn.eq("Y")
                )
                .fetch()
                .size();
    }

    private String aesDecode(String str) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String decrypt = aes.decrypt(str);
        return decrypt;
    }

    private BooleanExpression betweenDate(RiderCsReq riderCsReq){
        if(riderCsReq.getStartDate() == null || riderCsReq.getEndDate() == null) return null;

        return rider.createdDate.between(riderCsReq.getStartDate(), riderCsReq.getEndDate().plusDays(1));
    }

    private BooleanExpression callBetweenDate(CallsListReq callsListReq){
        if(callsListReq.getStartDate() == null || callsListReq.getEndDate() == null) return null;

        return call.callPickUpTime.between(callsListReq.getStartDate(), callsListReq.getEndDate().plusDays(1));
    }
}
