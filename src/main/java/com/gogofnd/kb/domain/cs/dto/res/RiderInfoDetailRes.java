package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderInfoDetailRes {
        private String name;
        private String phone;
        private String birthDate;
        private String vcnoHnglNm;
        private String driverId;
        private String region;
        private String loginId;
        private String status;
        private String mtdt;
        private String oprnPurp;
        private LocalDateTime effectiveStartDate;
        private LocalDateTime effectiveEndDate;
        private String policyNumber;
        private LocalDateTime createdDate;
        private LocalDateTime deletedDate;
        private LocalDateTime modifiedDate;
        private String sellerName;
        private String totalWebViewUrl;
        private String applicationNumber;
        private String useYn;
        private String payStatus;
}