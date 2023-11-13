package com.gogofnd.kb.domain.cs.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsureHistoryReq {
    // 이름
    private String name;
    // 생년월일
    private String birthDate;
    // 휴대폰번호
    private String phone;
    // 가입상태
    private String insureStatus;
    // 운영사
    private String sellerName;
    // 사용여부
    private String useYn;
    // 가입일 (start)
    private LocalDateTime startDate;
    // 가입일 (end)
    private LocalDateTime endDate;
}