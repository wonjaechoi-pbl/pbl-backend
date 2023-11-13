package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbBalanceHistoryRes {
    // 업체구분코드
    private String cmpcd;
    // 운영사
    private String sellerName;
    // 차감액
    private Integer useAmt;
    // 남은금액
    private Integer balance;
    // 처리일시
    private LocalDate date;
}