package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderInfoDetailRes {
        // 이름
        private String name;
        // 휴대폰번호
        private String phone;
        // 생년월일
        private String birthDate;
        // 이륜차번호
        private String vcnoHnglNm;
        // 보험사ID
        private String driverId;
        // 지역
        private String region;
        // 플랫폼ID
        private String loginId;
        // 가입상태
        private String status;
        // 의무보험만기일자
        private String mtdt;
        // 운행용도
        private String oprnPurp;
        // 보험적용시작일자
        private LocalDateTime effectiveStartDate;
        // 보험적용종료일자
        private LocalDateTime effectiveEndDate;
        // 증권번호
        private String policyNumber;
        // 생성일자
        private LocalDateTime createdDate;
        // 삭제일자
        private LocalDateTime deletedDate;
        // 수정일자
        private LocalDateTime modifiedDate;
        // 운영사명
        private String sellerName;
        // 웹뷰URL
        private String totalWebViewUrl;
        // 채권번호
        private String applicationNumber;
        // 사용여부
        private String useYn;
        // 충전여부
        private String payStatus;
}