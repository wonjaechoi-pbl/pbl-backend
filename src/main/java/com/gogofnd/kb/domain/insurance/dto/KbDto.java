package com.gogofnd.kb.domain.insurance.dto;


import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class KbDto {

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String phone;
    private String name;
    private String ssn_birthdate;
    private int gender;
    private String mtdt;
    private String oprn_purp;


    public KbDto(Rider rider) {
        this.proxy_driv_coorp_cmpcd = rider.getSeller().getCmpcd();
        this.driver_id = rider.getDriver_id();
        this.phone = rider.getPhone();
        this.name = rider.getName();
        this.gender = rider.getGender();
        this.mtdt = rider.getMtdt();
        this.oprn_purp = rider.getOprn_purp();
        this.ssn_birthdate = rider.getSsn();
    }

    public void updateSsn_birthdate(String ssn_birthdate) {
        this.ssn_birthdate = ssn_birthdate;
    }
}
