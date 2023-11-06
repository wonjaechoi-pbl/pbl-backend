package com.gogofnd.kb.domain.insurance.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reject_message")
@Entity
public class RejectMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "reject_message")
    private String rejectMessage;

}
