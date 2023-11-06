package com.gogofnd.kb.domain.insurance.dto.req;


import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApi10thReq {

    private Long id;

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String call_id;
    private String policy_number;
    private long start_time;
    private boolean restart; // false로 고정. 특이케이스에만 트루로 다시 kb쪽으로 보냄

    public KbApi10thReq(Rider rider, String call_id, LocalDateTime pickupTime){
        this.driver_id = rider.getDriver_id();
        this.proxy_driv_coorp_cmpcd = rider.getSeller().getCmpcd();
        this.call_id = call_id;
        this.policy_number = rider.getSeller().getPolicy_number();
        this.start_time = Timestamp.valueOf(pickupTime).getTime()/1000;
        this.restart = false;
    }

}
