package com.gogofnd.kb.domain.insurance.dto.req;

import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApi4thReq {

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String phone;
    private String vcno_hngl_nm;
    private List<String> application_numbers;

    public KbApi4thReq(Rider rider){
        this.proxy_driv_coorp_cmpcd = rider.getSeller().getCmpcd();
        this.driver_id = rider.getDriver_id();
        this.phone = rider.getPhone();
        this.vcno_hngl_nm = rider.getVcNumber();
        this.application_numbers = List.of(rider.getSeller().getApplication_number());
    }

}
