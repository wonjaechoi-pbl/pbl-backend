package com.gogofnd.kb.domain.rider.api;

import com.gogofnd.kb.domain.rider.dto.req.LoginReq;
import com.gogofnd.kb.domain.rider.dto.res.LoginRes;
import com.gogofnd.kb.domain.rider.service.RiderService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


// 앱이 개발이 안되있는데, 일단 안쓸것같음
@RequestMapping("/app/auth")
@RequiredArgsConstructor
@RestController
public class AuthApi {
    private final RiderService riderService;

    @PostMapping("/login")
    @ApiOperation(value = "로그인")
    public LoginRes login(@Valid @RequestBody LoginReq dto, HttpServletResponse res){
        return riderService.login(dto,res);
    }

    @GetMapping("/duplicate/{phone}")
    @ApiOperation(value = "아이디(전화번호) 중복체크")
    public String duplicateCheck(@PathVariable String phone){
        return riderService.duplicateCheck(phone);
    }
}
