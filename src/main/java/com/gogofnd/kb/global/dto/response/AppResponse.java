package com.gogofnd.kb.global.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppResponse {
    private ApiHeaderResponse header;
    private Map<String,String> data;
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
class te{
    private String appVer;
    private String sensorVersion;

    public String getAppVer() {
        return appVer;
    }
}

