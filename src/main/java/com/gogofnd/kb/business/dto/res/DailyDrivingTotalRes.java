package com.gogofnd.kb.business.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DailyDrivingTotalRes {
    private String total_count;
    private String total_minute;
    private String total_balance;
}
