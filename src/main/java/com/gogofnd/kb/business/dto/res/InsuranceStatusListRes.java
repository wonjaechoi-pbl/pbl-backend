package com.gogofnd.kb.business.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InsuranceStatusListRes {
    private Long id;
    private String name;
    private String vcNumber;
    private String driver_id;
    private String region;
    private String loginId;
    private String insuranceStatus;
    private String mtdt;
    private String oprn_purp;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String rejectReason;
}
