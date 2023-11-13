package com.gogofnd.kb.domain.cs.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerReq {
    // 운영사
    private String sellerName;
    // 대표자명
    private String bossName;
    // 보험종류
    private String InsureType;
}