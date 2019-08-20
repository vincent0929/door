package com.vc.door.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vc.door.core.constant.BizErrorEnum;
import com.vc.door.core.constant.DoorConstants;
import com.vc.door.core.constant.IdentityTypeEnum;
import com.vc.door.core.service.LoginService;
import io.github.vincent0929.common.constant.BaseEnum;
import io.github.vincent0929.common.dto.ResultDTO;
import io.github.vincent0929.common.util.BizAssert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.vc.door.core.constant.DoorConstants.COOKIE_TOKEN;

@Slf4j
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${door.login.home}")
    private String home;

    @RequestMapping("/valid")
    public ResultDTO valid(@RequestParam("ticket") String ticket, @RequestParam("appToken") String appToken,
                           @RequestParam("app") String app, @RequestParam("appKey") String appKey,
                           @RequestParam("appSecret") String appSecret) {
        return ResultDTO.success(loginService.validTicket(ticket, app, appToken, appKey, appSecret));
    }

    @PostMapping("/login")
    public void login(@RequestParam("identifier") String identifier,
                      @RequestParam("credential") String credential,
                      @RequestParam(value = "identityType", defaultValue = "1") Integer identityType,
                      @RequestParam("callback") String callback,
                      HttpServletResponse response) throws IOException {
        IdentityTypeEnum identityTypeEnum = BaseEnum.valueOf(IdentityTypeEnum.class, identityType);
        String token = loginService.login(identifier, credential, identityTypeEnum);
        response.addCookie(new Cookie(COOKIE_TOKEN, token));
        if (StringUtils.isNotBlank(callback)) {
            String ticket = loginService.grantTicket(token);
            response.sendRedirect(callback + "?" + DoorConstants.TICKET + "=" + ticket);
        } else {
            response.sendRedirect(home);
        }
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(objectMapper.writeValueAsString(ResultDTO.success(token)).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    @PostMapping("/logout")
    public ResultDTO logout(@RequestHeader(COOKIE_TOKEN) String token,
                            @RequestParam("appToken") String appToken) {
        BizAssert.isTrue(StringUtils.isNotBlank(token) || StringUtils.isNotBlank(appToken),
                BizErrorEnum.USER_NOT_LOGIN);
        if (StringUtils.isNotBlank(token)) {
            loginService.logoutByToken(token);
            return ResultDTO.success(true);
        }
        loginService.logoutByAppToken(appToken);
        return ResultDTO.success(true);
    }
}
