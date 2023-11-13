package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerRes {
    // 업체구분코드
    private String cmpcd;
    // 운영사
    private String sellerName;
    // 대표자명
    private String bossName;
    // 사업자번호
    private String businessNumber;
    // 주소
    private String address;
    // 채권번호
    private String applicationNumber;
    // 증권번호
    private String policyNumber;
    // 예납금잔액
    private Long balance;
    // 보험종류
    private String InsureType;
}