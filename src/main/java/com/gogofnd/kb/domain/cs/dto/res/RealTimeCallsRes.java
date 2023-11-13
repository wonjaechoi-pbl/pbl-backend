package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RealTimeCallsRes {
    // 운영사
    private String sellerName;
    // 이름
    private String name;
    // 운행 ID
    private String callId;
    // 정산(그룹) ID
    private String groupId;
    // 배차시간
    private LocalDateTime startDateTime;
    // 종료시간
    private LocalDateTime endDateTime;
    // 운행상태
    private String deliveryStatus;
    // 보험사운행 ID
    private String kbCallId;
}