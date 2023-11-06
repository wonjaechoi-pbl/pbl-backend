package com.gogofnd.kb.business.dto.res;

import com.gogofnd.kb.business.dto.req.Kb11thRequest;
import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.domain.seller.entity.CallSettlement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DrivingEndRes {
    private String driver_rundate;
    private String driver_enddate;
    private Integer driving_balance;
    private String insure_status;
    private String insure_message;
    private String total_time;
    
    // 온나/딜버 용
    public DrivingEndRes(Double drivingTime, Integer driving_balance, LocalDateTime endTime, String insure_status, String insure_message, CallSettlement callSettlementl) {
        this.driver_rundate = drivingTime.toString();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.driver_enddate = endTime.format(formatter);
        this.driving_balance = driving_balance;
        this.insure_status = insure_status;
        this.insure_message = insure_message;
        Duration total = Duration.between(callSettlementl.getCallPickUpTime(),callSettlementl.getCallCompleteTime());
        this.total_time = total.getSeconds() +"";
        System.out.println("total_time : " + total);
    }

    // 부릉 용
    public DrivingEndRes(LocalDateTime endTime, String insure_status, String insure_message) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.driver_enddate = endTime.format(formatter);
        this.insure_status = insure_status;
        this.insure_message = insure_message;
    }
}
