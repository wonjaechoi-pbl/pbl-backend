package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoDetailRes {
    // SEQ
    private Long id;
    // 플랫폼ID
    private String loginId;
    // 내용
    private String content;
    // 작성자
    private String writer;
    // 생성일자
    private LocalDateTime createdDate;
    // 수정일자
    private LocalDateTime modifiedDate;
}