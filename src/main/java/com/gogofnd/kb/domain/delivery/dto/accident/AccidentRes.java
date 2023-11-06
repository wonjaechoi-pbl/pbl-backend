package com.gogofnd.kb.domain.delivery.dto.accident;


import com.gogofnd.kb.domain.delivery.entity.Accident;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@NoArgsConstructor
@Getter
public class AccidentRes {
    private int total_count;
    private List<AccidentDetailRes> result;

    public AccidentRes(int total_count, List<AccidentDetailRes> result) {
        this.total_count = total_count;
        this.result = result;
    }
}

