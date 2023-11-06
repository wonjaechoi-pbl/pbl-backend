package com.gogofnd.kb.domain.rider.repository;

import com.gogofnd.kb.domain.insurance.dto.req.KbApiWebReq;
import com.gogofnd.kb.domain.rider.entity.RiderWeb;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.gogofnd.kb.domain.rider.entity.QRiderWeb.riderWeb;
import static com.gogofnd.kb.domain.seller.entity.QSeller.seller;

@RequiredArgsConstructor
@Repository
public class RiderWebRepositorySupport {
    private final JPAQueryFactory queryFactory;
    public RiderWeb findByDriverId(String id){
        RiderWeb resultRiderWeb = queryFactory
                .selectFrom(riderWeb)
                .where(riderWeb.driver_id.eq(id))
                .fetchOne();

        if(resultRiderWeb == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRiderWeb;
    }

    public RiderWeb findByLoginId(String id){
        RiderWeb resultRiderWeb = queryFactory
                .selectFrom(riderWeb)
                .where(riderWeb.loginId.eq(id))
                .fetchOne();

        if(resultRiderWeb == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRiderWeb;
    }

    public RiderWeb findByDriverIdAndSellerCode(String driverId, String sellerCode){
        RiderWeb resultRiderWeb = queryFactory
                .selectFrom(riderWeb)
                .where(riderWeb.driver_id.eq(driverId), riderWeb.seller.sellerCode.eq(sellerCode))
                .fetchOne();
        if(resultRiderWeb == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
        return resultRiderWeb;
    }

    public RiderWeb findByLoginIdAndSellerCode(String driverId, String sellerCode){
        RiderWeb resultRiderWeb = queryFactory
                .selectFrom(riderWeb)
                .where(riderWeb.loginId.eq(driverId), riderWeb.seller.sellerCode.eq(sellerCode))
                .fetchOne();
        if(resultRiderWeb == null){
            throw new BusinessException(ErrorCode.LOGIN_ID_OR_SELLERCODE_MISMATCH);
        }
        return resultRiderWeb;
    }

    public RiderWeb findByPhone(String phone){
        RiderWeb result = queryFactory.selectFrom(riderWeb)
                .where(riderWeb.phone.eq(phone))
                .fetchOne();

        return result;
    }

    public List<RiderWeb> findAllById(List<Long> id){

        List<RiderWeb> resultRiderWeb = queryFactory
                .selectFrom(riderWeb)
                .where(riderWeb.id.in(id))
                .fetch();
        return resultRiderWeb;
    }


    public List<RiderWeb> findAllByDriverId(List<String> driver_id){

        List<RiderWeb> resultRiderWeb = queryFactory
                .selectFrom(riderWeb)
                .where(riderWeb.driver_id.in(driver_id))
                .groupBy(riderWeb.driver_id)
                .fetch();
        return resultRiderWeb;
    }

    public RiderWeb save(RiderWeb riderWeb) {
        return riderWeb;
    }

    public List<KbApiWebReq> findAllRiderInfo(String cmpcd, String riderId) {

        BooleanBuilder builder = new BooleanBuilder();

        if (!StringUtils.isEmpty(riderId)) {
            builder.and(riderWeb.id.eq(Long.valueOf(riderId)));
        }

        List<KbApiWebReq> resultList = queryFactory
                .select(
                        Projections.fields(
                                KbApiWebReq.class,
                                riderWeb.loginId.as("driver_id"),
                                riderWeb.name.as("driver_name"),
                                riderWeb.phone.as("driver_phone"),
                                riderWeb.region.as("driver_region"),
                                riderWeb.ssn.as("driver_ssn"),
                                seller.sellerCode.as("seller_code"),
                                riderWeb.gender.as("driver_gender"),
                                riderWeb.vcNumber.as("driver_vcnum"),
                                riderWeb.mtdt.as("mtdt"),
                                riderWeb.oprn_purp.as("oprn_purp")
                        )
                )
                .from(riderWeb)
                .leftJoin(seller).on(riderWeb.seller.id.eq(seller.id))
                .where(
                        seller.cmpcd.eq(cmpcd),
                        riderWeb.insuranceStatus.eq("062"),
                        riderWeb.useYn.eq("Y"),
                        builder
                )
                .fetch();

        return resultList;
    }

    public String findBySellerCmpcd(String sellerCode){

        return queryFactory
                .select(seller.cmpcd)
                .from(seller)
                .where(seller.sellerCode.eq(sellerCode))
                .fetchOne();
    }
}
