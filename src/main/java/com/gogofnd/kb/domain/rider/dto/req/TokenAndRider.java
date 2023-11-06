package com.gogofnd.kb.domain.rider.dto.req;

import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class TokenAndRider extends RiderDto {
    private String token;

    public TokenAndRider(String token, Rider rider) {
        super(rider);
        this.token = token;
    }
}
