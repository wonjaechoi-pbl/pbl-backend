package com.gogofnd.kb.global.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private ApiHeaderResponse header;
    private T data;
    public ApiResponse(T data){
        this.header = new ApiHeaderResponse("200", "요청 완료 되었습니다.");
        this.data = data;
    }}
