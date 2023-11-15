package com.gogofnd.kb.domain.cs.dto.res;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsureHistoryRes {
    // SEQ
    private Long id;
    // 업체구분코드
    private String cmpcd;
    // 운영사명
    private String sellerName;
    // 휴대폰번호
    private String phone;
    // 이름
    private String name;
    // 의무보험만기일자
    private String mtdt;
    // 보험사 ID
    private String driverId;
    // 플랫폼 ID
    private String loginId;
    // 이륜차번호
    private String vcnoHnglNm;
    // 생년월일
    private String birthDate;
    // 생성일자
    private LocalDateTime createdDate;
    // 가입상태
    private String insuranceStatus;
    // 증권파일경로
    private String imagePath;
    // 가입상태명
    private String statusName;
    // 거절사유
    private String rejectMessage;
    // 메모내용
    private String memo;
    // 메모작성자
    private String memoWriter;
    // 웹뷰URL
    private String totalWebViewUrl;
    // 사용여부
    private String useYn;
}
