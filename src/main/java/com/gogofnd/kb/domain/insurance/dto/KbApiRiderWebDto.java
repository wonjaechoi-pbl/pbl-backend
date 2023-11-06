package com.gogofnd.kb.domain.insurance.dto;


import com.gogofnd.kb.domain.rider.entity.RiderWeb;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class KbApiRiderWebDto {

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String phone;
    private String name;
    private String ssn_birthdate;
    private int gender;
    private String mtdt;
    private String oprn_purp;



    public KbApiRiderWebDto(RiderWeb riderWeb) {
        this.proxy_driv_coorp_cmpcd = riderWeb.getSeller().getCmpcd();
        this.driver_id = riderWeb.getDriver_id();
        this.phone = riderWeb.getPhone();
        this.name = riderWeb.getName();
        this.gender = riderWeb.getGender();
        this.mtdt = riderWeb.getMtdt();
        this.oprn_purp = riderWeb.getOprn_purp();
        this.ssn_birthdate = riderWeb.getSsn();
    }

    public void updateSsn_birthdate(String ssn_birthdate) {
        this.ssn_birthdate = ssn_birthdate;
    }
}
