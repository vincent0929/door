package com.vc.door.client.service;

import com.vc.door.client.dto.TicketValidResponse;
import io.github.vincent0929.common.dto.ResultDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface SsoLoginService {

    boolean validToken(String token);

    ResultDTO<TicketValidResponse> validTicket(String ticket);

    ResultDTO<Boolean> logout(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
