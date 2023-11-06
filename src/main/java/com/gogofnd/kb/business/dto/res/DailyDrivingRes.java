package com.gogofnd.kb.business.dto.res;

import com.gogofnd.kb.domain.seller.entity.Call;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Getter
public class DailyDrivingRes {
    private String start_date;
    private String end_date;
    private long balance;

    @Builder
    public DailyDrivingRes(String startDate, String end_date, long balance) {
        this.start_date = startDate;
        this.end_date = end_date;
        this.balance = balance;
    }
}
