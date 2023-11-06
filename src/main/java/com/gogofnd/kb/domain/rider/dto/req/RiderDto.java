package com.gogofnd.kb.domain.rider.dto.req;

import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RiderDto {
    private String name;
    private String phone;
    private String vcNumber;
    private int gender;
    private String applicationNumber;
    private Long seller_id;

    private String password;

    public RiderDto(Rider rider) {
        this.name = rider.getName();
        this.phone = rider.getPhone();
        this.gender = rider.getGender();
        this.vcNumber = rider.getVcNumber();
        this.applicationNumber = rider.getApplicationNumber();
    }
}
