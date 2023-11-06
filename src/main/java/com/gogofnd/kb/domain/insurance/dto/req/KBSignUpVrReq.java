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
public class KBSignUpVrReq extends KbSignUpReq{

    private String policy_number;

    public KBSignUpVrReq(Rider rider){
        super(rider);
        this.policy_number = rider.getSeller().getPolicy_number();
    }
}
