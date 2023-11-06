package com.gogofnd.kb.domain.rider.dto.res;

import com.gogofnd.kb.domain.rider.entity.ApplyStatus;
import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RiderStatusRes {
    private String insurance_status;
    private String apply_status;

    public RiderStatusRes(Rider rider) {
        this.insurance_status = rider.getInsuranceStatus();
        this.apply_status = rider.getApplyStatus().toString();
    }
}
