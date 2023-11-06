package com.gogofnd.kb.domain.rider.dto.res;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RiderIdSsnRes {
    private Long id;
    private String ssn;
    private String birthDate;
}
