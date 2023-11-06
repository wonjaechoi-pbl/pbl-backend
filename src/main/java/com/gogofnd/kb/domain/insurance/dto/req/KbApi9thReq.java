package com.gogofnd.kb.domain.insurance.dto.req;


import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.seller.entity.Seller;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApi9thReq {

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private Integer start_day;
    private String policy_number;


    public KbApi9thReq(Rider rider){
        this.proxy_driv_coorp_cmpcd = rider.getSeller().getCmpcd();
        this.driver_id = rider.getDriver_id();
        this.policy_number = rider.getSeller().getPolicy_number();
        this.start_day = (int) System.currentTimeMillis() / 1000;

    }
}
