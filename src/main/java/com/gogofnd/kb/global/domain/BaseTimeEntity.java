package com.gogofnd.kb.global.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;


//엔티티들 얘 상속받아야함
@Setter
@Getter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate // 엔티티 생성시 자동으로 날짜 들어감
    private LocalDateTime createdDate;

    @LastModifiedDate // 엔티티 수정시 자동으로 날짜 들어감
    private LocalDateTime modifiedDate;

    private LocalDateTime deletedDate;
}