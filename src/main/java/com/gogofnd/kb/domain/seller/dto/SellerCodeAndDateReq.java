package com.gogofnd.kb.domain.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SellerCodeAndDateReq {
    private String seller_code;
    private String seller_startdate;
    private String seller_enddate;
}
