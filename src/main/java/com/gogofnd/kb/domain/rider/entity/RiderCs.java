package com.gogofnd.kb.domain.rider.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rider_cs")
@Entity
public class RiderCs {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rider")
    private Long rider;
    @Column(name = "group_name")
    private String group_name;
    @Column(name = "phone")
    private String phone;
    @Column(name = "name")
    private String name;
    @Column(name = "rider_id")
    private String rider_id;
    @Column(name = "birthDate")
    private String birthDate;
    @Column(name = "insuranceStatus")
    private String insuranceStatus;
    @Column(name = "createdDate")
    private LocalDateTime createdDate;
    @Column(name = "reject_reason")
    private String reject_reason;
    @Column(name = "reject_message")
    private String reject_message;
    @Column(name = "memo")
    private String memo;
}
