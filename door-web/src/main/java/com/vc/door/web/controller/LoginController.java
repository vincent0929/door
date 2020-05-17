package com.vc.door.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vc.door.core.constant.BizErrorEnum;
import com.vc.door.core.constant.DoorConstants;
import com.vc.door.core.constant.IdentityTypeEnum;
import com.vc.door.core.entity.UserDO;
import com.vc.door.core.service.LoginService;
import io.github.vincent0929.common.constant.BaseEnum;
import io.github.vincent0929.common.dto.ResultDTO;
import io.github.vincent0929.common.util.BizAssert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.vc.door.core.constant.DoorConstants.COOKIE_TOKEN;

@Slf4j
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${door.login.index}")
    private String index;

    @GetMapping("/valid")
    public ResultDTO<Long> valid(@RequestParam("ticket") String ticket, @RequestParam("appToken") String appToken,
                                 @RequestParam("appName") String appName, @RequestParam("appKey") String appKey,
                                 @RequestParam("appSecret") String appSecret) {
        return ResultDTO.success(loginService.validTicket(ticket, appName, appToken, appKey, appSecret));
    }

    @RequestMapping("/dologin")
    public void doLogin(@RequestParam("identifier") String identifier,
                        @RequestParam("credential") String credential,
                        @RequestParam(value = "identityType", defaultValue = "1") Integer identityType,
                        @RequestParam("callback") String callback,
                        HttpServletResponse response) throws IOException {
        IdentityTypeEnum identityTypeEnum = BaseEnum.valueOf(IdentityTypeEnum.class, identityType);
        ResultDTO<String> tokenResult = loginService.login(identifier, credential, identityTypeEnum);
        if (!tokenResult.isSuccess()) {
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(objectMapper.writeValueAsString(tokenResult).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            return;
        }

        String token = tokenResult.getData();
        response.addCookie(new Cookie(COOKIE_TOKEN, token));
        String ticket = loginService.grantTicket(token);
        response.addCookie(new Cookie(DoorConstants.TICKET, ticket));
        if (callback == null || callback.isEmpty()) {
            response.sendRedirect(index);
        } else {
            response.sendRedirect(URLDecoder.decode(callback, StandardCharsets.UTF_8.displayName()));
        }
    }

    @GetMapping("/logout")
    public ResultDTO<Boolean> logout(@RequestParam("appName") String appName, @RequestParam("token") String token) {
        BizAssert.isTrue(StringUtils.isNotBlank(token), BizErrorEnum.USER_NOT_LOGIN);
        BizAssert.isTrue(StringUtils.isNotBlank(appName), BizErrorEnum.APP_NAME_NOT_BLANK);
        loginService.logoutByToken(appName, token);
        return ResultDTO.success(true);
    }

    @GetMapping("/index")
    public ResultDTO<UserDO> index(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (ArrayUtils.isEmpty(cookies)) {
            return ResultDTO.fail(500, "error");
        }
        Cookie tokenCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(COOKIE_TOKEN))
                .findFirst().orElse(null);
        if (tokenCookie == null) {
            return ResultDTO.fail(500, "error");
        }
        String token = tokenCookie.getValue();
        return ResultDTO.success(loginService.getUser(token));
    }
}
