package com.gogofnd.kb.domain.delivery.dto.app.res;

import com.gogofnd.kb.domain.delivery.entity.BalanceHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceStatusRes {
    private Integer balance;
    private String  balanceStatus;
    private LocalDateTime createDate;

    public BalanceStatusRes(BalanceHistory balanceHistory) {
        this.balance = balanceHistory.getBalance();
        this.balanceStatus = balanceHistory.getBalanceStatus();
        this.createDate = balanceHistory.getCreatedDate();
    }
}
