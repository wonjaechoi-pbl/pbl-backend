package com.gogofnd.kb.domain.insurance.dto.req;


import com.gogofnd.kb.domain.seller.entity.Call;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApi11Req {

    private Long id;
    private String  proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String call_id;
    private long end_time;
    private String auto_end;
    private String balance;
    private String seller_code;


    public KbApi11Req(Call call){
        this.proxy_driv_coorp_cmpcd = call.getRider().getSeller().getCmpcd();
        this.driver_id = call.getRider().getDriver_id();
        this.end_time = Timestamp.valueOf(LocalDateTime.now()).getTime()/1000;
        this.call_id = call.getKb_call_id();
        this.auto_end = "N";
    }

    public void changeBalance(String balance) {
        this.balance = balance;
    }
}
