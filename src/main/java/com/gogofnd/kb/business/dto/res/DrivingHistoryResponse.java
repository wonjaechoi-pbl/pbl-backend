package com.gogofnd.kb.business.dto.res;


import com.gogofnd.kb.domain.seller.entity.Call;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class DrivingHistoryResponse {

    private String call_id;
    private String driver_startdate;
    private String driver_enddate;

    public DrivingHistoryResponse(Call call){
        this.call_id = call.getCallId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        this.driver_enddate = call.getCallCompleteTime().format(formatter);
        this.driver_startdate = call.getCallAppointTime().format(formatter);
    }

}
