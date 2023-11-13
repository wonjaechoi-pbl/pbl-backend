package com.gogofnd.kb.domain.cs.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RealTimeCallsReq {
    // 현재 페이지
    private Integer page;
    // 페이지 당 row 수
    private Integer limit;
    // 운행 ID
    private String callId;
    // 정산(그룹) ID
    private String groupId;
    // 이름
    private String name;
    // 운행상태
    private String deliveryStatus;
    // 운영사
    private String sellerName;
    // 배달시작시간
    private LocalDateTime startDateTime;
    // 배달종료시간
    private LocalDateTime endDateTime;
}