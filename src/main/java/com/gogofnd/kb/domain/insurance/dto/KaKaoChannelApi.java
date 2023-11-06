package com.gogofnd.kb.domain.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class KaKaoChannelApi {

    public String templateCode;
    public String reserve;
    public String sendDate;
    public String reSend;
    public String resendType;
    public String resendTitle;
    public String resendContent;
    public List<Root> list = new ArrayList<>();

    public void makeList(String phone,List<String> param){
       this.list.add(new Root(phone,param));
    }
}


@Getter
@AllArgsConstructor
 class Root {
    public String phone;
    public List<String> templateParam;
}
