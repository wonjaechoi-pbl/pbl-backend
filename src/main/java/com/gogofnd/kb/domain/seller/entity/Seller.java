package com.gogofnd.kb.domain.seller.entity;

import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.global.domain.BaseTimeEntity;
import com.querydsl.codegen.GenericExporter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "seller")
public class Seller extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_code", length = 20)
    private String sellerCode;
    @Column(length = 50)
    private String name;
    @Column(length = 20)
    private String tell; //지점 전화번호
    @Column(length = 20)
    private String phone;
    private long balance;
    private String seller_UID; // seller 식별자
    private int chargingPerHour;

    private String bossName;
    private String businessNumber;
    private String address;

    private String apiKey;

    private String policy_number;

    private String application_number;

    private String cmpcd;

    private Double first_bd_fee;

    private Double second_bd_fee;

    private Double pd_fee;

    private Double flat_rate;

    private Double discount_rate;

    @Column(name = "insure_type",length = 1)
    private String insureType;

    public void updateBalance(long balance) {
        this.balance += balance;
    }

    public void dischargeBalance(long balance) {
        this.balance -= balance;
    }

    public void refundBalance(long balance) {
        this.balance += balance;
    }


}
