package com.gogofnd.kb.domain.delivery.dto.accident;

import com.gogofnd.kb.domain.delivery.entity.Accident;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AccidentDetailRes {
    private String accident_time;
    private String claim_number;

    public AccidentDetailRes(Accident accident) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = accident.getAccident_time().format(formatter);

        this.accident_time = time;
        this.claim_number = accident.getClaimNumber();
    }
}
