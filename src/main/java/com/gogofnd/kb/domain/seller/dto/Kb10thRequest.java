package com.gogofnd.kb.domain.seller.dto;

import com.gogofnd.kb.domain.seller.entity.Call;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Kb10thRequest {
    @Size(max = 30)
    private String call_id;
    private String driver_pickupaddress;
    private String driver_deliveryaddress;
    private String driver_client;
    private String call_requesttime;
    private String call_appointtime;
    private String company_name;
    private String pickup_time;
    @Size(max = 30)
    private String driver_id;
    private String seller_code;

    public Kb10thRequest(Call call) {
        this.call_id = call.getKb_call_id();
        this.driver_id = call.getRider().getDriver_id();
    }
}
