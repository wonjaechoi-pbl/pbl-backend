package com.gogofnd.kb.domain.insurance.dto;


import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApiRiderDto {

    private Long id;
    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String name;
    private String ssn;
    private String vcno_hngl_nm;

    private String region;
    private String oprn_purp;
    private String mtdt;

    public KbApiRiderDto(Rider rider){
        String setMtdt = "";
        String setOprnPurp = "";

        if(rider.getMtdt() != null)
            setMtdt = rider.getMtdt();

        if (rider.getOprn_purp() != null)
            setOprnPurp = rider.getOprn_purp();

        this.proxy_driv_coorp_cmpcd = rider.getSeller().getCmpcd();
        this.driver_id = rider.getDriver_id();
        this.name = rider.getName();
        this.ssn = rider.getSsn();
        this.vcno_hngl_nm = rider.getVcNumber();
        this.region = rider.getRegion();
        this.oprn_purp = setOprnPurp;
        this.mtdt = setMtdt;
    }

    public void updateSsn(String ssn) {
        this.ssn = ssn;
    }
}
