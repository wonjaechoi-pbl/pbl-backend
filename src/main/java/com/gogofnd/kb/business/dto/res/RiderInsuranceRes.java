package com.gogofnd.kb.business.dto.res;

import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RiderInsuranceRes {
    private String insuranceStatus;

    public RiderInsuranceRes(Rider rider) {
        this.insuranceStatus = rider.getInsuranceStatus();
    }
}
