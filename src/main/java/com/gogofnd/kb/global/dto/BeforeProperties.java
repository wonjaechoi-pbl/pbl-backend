package com.gogofnd.kb.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Validated
@AllArgsConstructor
//@ConstructorBinding
@ConfigurationProperties(prefix = "app")
@ConfigurationPropertiesScan
public class BeforeProperties {

    private String kbUrl;
}
