package com.vc.door.client.service;

import io.github.vincent0929.common.dto.ResultDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SsoLoginService {

    boolean validToken(String token);

    ResultDTO validTicket(String ticket);

    ResultDTO logout(HttpServletRequest request, HttpServletResponse response);
}
