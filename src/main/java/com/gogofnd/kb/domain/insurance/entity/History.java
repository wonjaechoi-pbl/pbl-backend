package com.gogofnd.kb.domain.insurance.entity;


import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.seller.entity.Seller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="insurance_history")
@Entity
@Builder
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "rider_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rider rider;
    @Column(length = 20)
    private String status;

    @Column(length = 20)
    private String applicationNumber;
    @Column(length = 20)
    private String policy_number;

    private LocalDateTime createdDate;
    private LocalDateTime effectiveStartDate;

    private LocalDateTime effectiveEndDate;
    private LocalDateTime until;

    @JoinColumn(name = "seller_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Seller seller;

    private String ageYn;

    public static History create(Rider rider, String status, Seller seller) {
        return History.builder()
                .createdDate(LocalDateTime.now())
                .status(status)
                .policy_number(seller.getPolicy_number())
                .applicationNumber(seller.getApplication_number())
                .rider(rider)
                .seller(seller)
                .build();
    }


    public void changeUntil(LocalDateTime until) {
        this.until = until;
    }

    public void updateEffectiveDate(LocalDateTime effectiveStartDate,LocalDateTime effectiveEndDate){
        this.effectiveEndDate = effectiveEndDate;
        this.effectiveStartDate = effectiveStartDate;
    }

    public static History create(Rider rider, String status, Seller seller,String ageYn){
        return History.builder()
                .createdDate(LocalDateTime.now())
                .status(status)
                .policy_number(seller.getPolicy_number())
                .applicationNumber(seller.getApplication_number())
                .rider(rider)
                .seller(seller)
                .ageYn(ageYn)
                .build();
    }

    public void updatePolicy_number(String policy_number) {
        this.policy_number = policy_number;
    }
}

