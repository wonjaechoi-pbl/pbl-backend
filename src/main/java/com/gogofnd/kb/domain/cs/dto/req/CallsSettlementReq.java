package com.gogofnd.kb.domain.cs.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallsSettlementReq {
    // 정산(그룹) ID
    private String groupId;
    // 이름
    private String name;
    // 운영사
    private String sellerName;
    // 정산여부
    private String settlementStatus;
    // 배달시작시간
    private String startDateTime;
    // 배달종료시간
    private String endDateTime;
}