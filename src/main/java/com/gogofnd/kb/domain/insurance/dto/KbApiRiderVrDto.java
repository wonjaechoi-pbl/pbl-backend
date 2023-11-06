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
public class KbApiRiderVrDto extends KbApiRiderDto{

    private String policy_number;

    public KbApiRiderVrDto(Rider rider) {
        super(rider);

        String setPolicyNumber = "";

        if (rider.getSeller().getPolicy_number() != null) {
            setPolicyNumber = rider.getSeller().getPolicy_number();
        }

        this.policy_number = setPolicyNumber;
    }
}
