package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerInfoDetailRes {
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Long balance;
    private String sellerName;
    private String sellerCode;
    private String address;
    private String bossName;
    private String businessNumber;
    private String policyNumber;
    private String applicationNumber;
    private String apiKey;
    private String cmpcd;
    private Double firstBdFee;
    private Double pdFee;
    private Double secondBdFee;
    private Double flatRate;
    private Double discountRate;
    private String InsureType;
}