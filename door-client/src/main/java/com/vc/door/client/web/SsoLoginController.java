package com.vc.door.client.web;

import com.vc.door.client.service.SsoLoginService;
import io.github.vincent0929.common.dto.ResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class SsoLoginController {

    @Autowired
    private SsoLoginService loginService;

    @RequestMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        loginService.logout(request, response);
    }
}
