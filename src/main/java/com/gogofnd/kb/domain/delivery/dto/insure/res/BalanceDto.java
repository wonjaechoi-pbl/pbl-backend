package com.gogofnd.kb.domain.delivery.dto.insure.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BalanceDto {
    private Integer balance;
    public BalanceDto(Integer balance) {
        this.balance = balance;
    }
}
