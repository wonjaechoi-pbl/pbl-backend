package com.gogofnd.kb.domain.rider.dto.res;

import com.gogofnd.kb.domain.rider.entity.RiderWeb;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RiderWebStatusRes {
    private String insurance_status;
    private String apply_status;

    public RiderWebStatusRes(RiderWeb riderWeb) {
        this.insurance_status = riderWeb.getInsuranceStatus();
        this.apply_status = riderWeb.getApplyStatus().toString();
    }
}
