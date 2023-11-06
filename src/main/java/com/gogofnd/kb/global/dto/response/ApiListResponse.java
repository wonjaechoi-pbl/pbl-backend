package com.gogofnd.kb.global.dto.response;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor()
@AllArgsConstructor
@Setter
public class ApiListResponse<T> {

    private ApiHeaderResponse header;

    private List<T> body;

}
