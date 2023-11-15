package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsureHistoryDetailRes {
    // 이름
    private String name;
    // 생성일자
    private LocalDateTime createdDate;
    // 가입상태
    private String status;
    // 가입상태명
    private String status_name;
    // 거절사유
    private String reject_message;
    // 보험적용시작일자
    private LocalDateTime effectiveStartDate;
    // 보험적용종료일자
    private LocalDateTime effectiveEndDate;
    // 심사결과유효기한
    private LocalDateTime until;
}
