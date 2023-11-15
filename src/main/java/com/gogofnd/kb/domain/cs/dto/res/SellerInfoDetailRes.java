package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerInfoDetailRes {
    // 생성일자
    private LocalDateTime createdDate;
    // 수정일자
    private LocalDateTime modifiedDate;
    // 예납금잔액
    private Long balance;
    // 운영사명
    private String sellerName;
    // 운영사코드
    private String sellerCode;
    // 주소
    private String address;
    // 대표자명
    private String bossName;
    // 사업자번호
    private String businessNumber;
    // 증권번호
    private String policyNumber;
    // 채권번호
    private String applicationNumber;
    // APIKEY
    private String apiKey;
    // 업체구분코드
    private String cmpcd;
    // 대인1
    private Double firstBdFee;
    // 대물
    private Double pdFee;
    // 대인2
    private Double secondBdFee;
    // 정액제
    private Double flatRate;
    // 할인율
    private Double discountRate;
    // 보험종류
    private String InsureType;
}