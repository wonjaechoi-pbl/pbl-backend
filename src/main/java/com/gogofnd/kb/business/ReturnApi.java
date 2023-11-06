package com.gogofnd.kb.business;

import com.gogofnd.kb.domain.insurance.service.InsuranceService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;


//카카오 웹뷰 리턴 받는 컨트롤러(리다이렉트용)
@RequiredArgsConstructor
@Controller
public class ReturnApi {
    private final InsuranceService insuranceService;

    @ApiOperation(value = "api1(가입설계동의) 리턴 url", notes = "가입설계 동의가 완료된 라이더의 경우, kb에서 이 url로 리턴을 해줍니다.")
    @GetMapping("/1/return&tel={tel}")
    public String api1Return(@PathVariable String tel){
        String strBoolean =  insuranceService.api1Return(tel);
        return "api1Success";
    }

    @ApiOperation(value = "api4(계약체결동의) 리턴 url", notes = "계약체결 이행 동의가 완료된 라이더의 경우, kb에서 이 url로 리턴을 해줍니다.")
    @GetMapping("/4/return&tel={tel}")
    public String api4Return(@PathVariable String tel){
        insuranceService.api4Return(tel);
        return "api4Success";
    }


    //카카오톡 보낼때
    @GetMapping("/kakao1")
    public RedirectView api1ShortCut(@RequestParam String phone){
        RedirectView redirectView = new RedirectView();
        String totalUrl = insuranceService.getTotalUrlApi1(phone);
        redirectView.setUrl(totalUrl);
        System.out.println("totalUrl2 = " + totalUrl);
        return redirectView;
    }

    @GetMapping("/kakao4")
    public RedirectView api4ShortCut(@RequestParam String phone){
        RedirectView redirectView = new RedirectView();
        String totalUrl = insuranceService.getTotalUrlApi4(phone);
        redirectView.setUrl(totalUrl);
        System.out.println("totalUrl = " + totalUrl);
        return redirectView;
    }
}
