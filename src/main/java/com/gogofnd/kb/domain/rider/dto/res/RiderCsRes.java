package com.gogofnd.kb.domain.rider.dto.res;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RiderCsRes {
    private Long id;
    private String cmpcd;
    private String group_name;
    private String phone;
    private String name;
    private String mtdt;
    private String driverId;
    private String loginId;
    private String vcNumber;
    private String birthDate;
    private LocalDateTime createdDate;
    private String insuranceStatus;
    private String imagePath;
    private String status_name;
    private String reject_message;
    private String memo;
    private String memoWriter;
    private String totalWebViewUrl;
    private String useYn;
}
