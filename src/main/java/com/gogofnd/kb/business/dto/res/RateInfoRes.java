package com.gogofnd.kb.business.dto.res;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RateInfoRes {
    private String seller_lossRate;
    private String seller_balance;

    public RateInfoRes(String seller_lossRate, String seller_balance) {
        this.seller_lossRate = seller_lossRate + "%";
        this.seller_balance = seller_balance;
    }
}
