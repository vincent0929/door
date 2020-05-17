package com.vc.door.test2.controller;

import io.github.vincent0929.common.dto.ResultDTO;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static com.vc.door.client.constant .SsoConstant.COOKIE_TOKEN;

@RestController
public class DoorTest2Controller {

    @GetMapping("/index")
    public ResultDTO<String> index(HttpServletRequest request) {
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
        return ResultDTO.success(token);
    }
}
