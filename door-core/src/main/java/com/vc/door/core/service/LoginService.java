package com.vc.door.core.service;

import com.vc.door.core.constant.IdentityTypeEnum;
import io.github.vincent0929.common.dto.ResultDTO;

public interface LoginService {

    boolean validToken(String token);

    Long validTicket(String ticket, String app, String appToken, String appKey, String appSecret);

    String login(String identifier, String credential, IdentityTypeEnum identityType);

    String grantTicket(String token);

    void logoutByToken(String token);

    void logoutByAppToken(String appToken);
}
