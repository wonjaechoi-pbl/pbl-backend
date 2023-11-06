package com.gogofnd.kb.domain.delivery.dto.insure.req;

import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.domain.seller.entity.CallSettlement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class DeliveryInsureHistoryReq {

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String call_id;
    private String policy_number;
    private Long start_time;
    private Long end_time;
    private String target_location;
//    private List<String> call_group_id;
    private String total_time = "0";
    public DeliveryInsureHistoryReq(CallSettlement callSettlement) {
        this.proxy_driv_coorp_cmpcd = callSettlement.getRider().getSeller().getCmpcd();
        this.driver_id = callSettlement.getRider().getDriver_id();
        this.call_id = callSettlement.getGroupId();
        this.policy_number = callSettlement.getRider().getSeller().getPolicy_number();
        this.start_time = Timestamp.valueOf(callSettlement.getCallPickUpTime()).getTime()/1000;
        if(callSettlement.getCallCompleteTime() != null){
            this.end_time = Timestamp.valueOf(callSettlement.getCallCompleteTime()).getTime()/1000;
        } else {
            this.end_time = Timestamp.valueOf(LocalDateTime.now()).getTime()/1000;
        }
        this.target_location = "-";
    }

    public void TotalTimeSetting(String totalTime){
        // wonjaechoi - 운행건 total_time 로그 확인
        System.out.println("total_time - " + totalTime);

        total_time = totalTime;
    }
//    public void CallGroupIdSetting(List<String> callGroupId) {
//        this.call_group_id = callGroupId;
//    }
}
