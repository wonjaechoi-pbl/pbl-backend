package com.gogofnd.kb.business.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateInsureDateReq {

    // 일보험시작일자
    private LocalDateTime effectiveStartDate;

    // 일보험종료일자
    private LocalDateTime effectiveEndDate;

    // KB라이더ID
    private String driverId;
}
