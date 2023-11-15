package com.gogofnd.kb.domain.cs.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCsMemoReq {
    // SEQ
    private Long id;
    // 내용
    private String content;
    // 작성자
    private String writer;
}