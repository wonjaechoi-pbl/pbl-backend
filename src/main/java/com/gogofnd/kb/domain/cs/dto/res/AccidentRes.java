package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccidentRes {
    // 사고접수번호
    private String claimNumber;
    // 운행 ID
    private String callId;
    // 이름
    private String name;
    // 운영사
    private String sellerName;
    // 사고접수일시
    private LocalDateTime claimTime;
    // 사고발생일시
    private LocalDateTime accidentTime;
}