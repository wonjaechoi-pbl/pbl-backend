package com.gogofnd.kb.domain.delivery.dto.insure.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BalanceInsureReq {
    private String proxy_driv_coorp_cmpcd; //proxy_driv_coorp_compcd 장난하나
    private String date;
    private String balance;
    private String use_amt;

    @Override
    public String toString() {
        return "BalanceInsureResquestDto{" +
                "proxy_driv_coorp_cmpcd='" + proxy_driv_coorp_cmpcd + '\'' +
                ", date='" + date + '\'' +
                ", balance='" + balance + '\'' +
                ", use_amt = '" + use_amt + '\'' +
                '}';
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setUse_amt(String use_amt){
        this.use_amt = use_amt;
    }
}
