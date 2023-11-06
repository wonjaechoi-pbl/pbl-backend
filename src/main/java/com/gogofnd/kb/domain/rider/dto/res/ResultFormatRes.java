package com.gogofnd.kb.domain.rider.dto.res;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResultFormatRes<T> {
    private T server_result;
    public ResultFormatRes(T server_result){
        this.server_result = server_result;
    }
}
