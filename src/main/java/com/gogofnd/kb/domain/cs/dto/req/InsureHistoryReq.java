package com.gogofnd.kb.domain.cs.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsureHistoryReq {
    private Integer page;
    private Integer limit;
    private String name;
    private String birthDate;
    private String phone;
    private String insureStatus;
    private String sellerName;
    private String useYn;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}