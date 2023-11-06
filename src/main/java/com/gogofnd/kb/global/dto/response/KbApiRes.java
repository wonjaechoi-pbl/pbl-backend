package com.gogofnd.kb.global.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class KbApiRes <T>{
    private Map<String,String> dataHeader;
    private T dataBody;

    public KbApiRes(T dataBody) {
        this.dataBody = dataBody;
        this.dataHeader = new HashMap<>();
    }
}

