package com.gogofnd.kb.domain.insurance.dto.req;


import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbSignUpReq {
    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String vcno_hngl_nm;
    private String name;
    private String ssn;
    private String policy_number;


    public KbSignUpReq(Rider rider){
        this.proxy_driv_coorp_cmpcd = rider.getSeller().getCmpcd();
        this.driver_id = rider.getDriver_id();
        this.vcno_hngl_nm = rider.getVcNumber();
        this.name = rider.getName();
        this.ssn = rider.getSsn();
        this.policy_number = rider.getSeller().getPolicy_number();
    }

    public void updateSsn(String ssn) {
        this.ssn = ssn;
    }
}
