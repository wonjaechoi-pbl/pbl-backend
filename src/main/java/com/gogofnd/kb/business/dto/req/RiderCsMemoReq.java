package com.gogofnd.kb.business.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RiderCsMemoReq {

    private String loginId;

    private String content;

    private String writer;
}
