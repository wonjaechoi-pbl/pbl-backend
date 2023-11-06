package com.gogofnd.kb.domain.delivery.dto.app.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BalanceUpdateRes<T> {
    private T result;
}
