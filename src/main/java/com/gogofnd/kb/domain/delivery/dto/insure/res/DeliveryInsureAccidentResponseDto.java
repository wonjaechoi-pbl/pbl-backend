package com.gogofnd.kb.domain.delivery.dto.insure.res;

import com.gogofnd.kb.domain.seller.entity.Call;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
public class DeliveryInsureAccidentResponseDto {
    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String policy_number;
    private String call_id;
    private Long call_request_time;

    private String start_location;
    private String target_location;

    @Override
    public String toString() {
        return "DeliveryInsureAccidentResponseDto{" +
                "proxy_driv_coorp_cmpcd='" + proxy_driv_coorp_cmpcd + '\'' +
                ", driver_id='" + driver_id + '\'' +
                ", policy_number='" + policy_number + '\'' +
                ", call_id='" + call_id + '\'' +
                ", call_request_time=" + call_request_time +
                ", start_location='" + start_location + '\'' +
                ", target_location='" + target_location + '\'' +
                '}';
    }


    public DeliveryInsureAccidentResponseDto (Call call){
        this.proxy_driv_coorp_cmpcd = call.getRider().getSeller().getCmpcd();
        this.driver_id = call.getRider().getDriver_id();
        this.call_id = call.getKb_call_id();
        this.policy_number = call.getRider().getSeller().getPolicy_number();
        this.start_location = call.getPickUp_Address();
        this.target_location = call.getDelivery_Address();
        this.call_request_time = Timestamp.valueOf(call.getCallRequestTime()).getTime()/1000;
    }
}
