package com.gogofnd.kb.domain.delivery.dto.insure.res;

import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.domain.seller.entity.CallSettlement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryInsureHistorykbResponseDto {
    private String proxy_driv_coorp_cmpcd;
    private String call_id;
    private String policy_number;
    private Long start_time;
    private Long end_time;

    @Override
    public String toString() {
        return "DeliveryInsureHistorykbResponseDto{" +
                "proxy_driv_coorp_cmpcd='" + proxy_driv_coorp_cmpcd + '\'' +
                ", call_id='" + call_id + '\'' +
                ", policy_number='" + policy_number + '\'' +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                '}';
    }

    public DeliveryInsureHistorykbResponseDto (CallSettlement callSettlement){
        String groupId = callSettlement.getGroupId().replaceAll("-","");

        this.proxy_driv_coorp_cmpcd = callSettlement.getRider().getSeller().getCmpcd();
        this.call_id = groupId;
        this.policy_number = callSettlement.getRider().getSeller().getPolicy_number();
        this.start_time = Timestamp.valueOf(callSettlement.getCallPickUpTime()).getTime()/1000;
        //Call 데이터 중 아직 운행중인 경우 예외 처리
        if(callSettlement.getCallCompleteTime() != null){
        this.end_time = Timestamp.valueOf(callSettlement.getCallCompleteTime()).getTime()/1000;
        } else {
        this.end_time = Timestamp.valueOf(LocalDateTime.now()).getTime()/1000;
        }
    }
}
