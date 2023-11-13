package com.gogofnd.kb.domain.cs.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbBalanceHistoryReq {
    // 운영사
    private String sellerName;
    // 처리일시
    private LocalDateTime date;
}