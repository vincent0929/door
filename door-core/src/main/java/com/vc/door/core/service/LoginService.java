package com.vc.door.core.service;

import com.vc.door.core.constant.IdentityTypeEnum;
import com.vc.door.core.entity.UserDO;
import io.github.vincent0929.common.dto.ResultDTO;

public interface LoginService {

    boolean validToken(String token);

    Long validTicket(String ticket, String appName, String appToken, String appKey, String appSecret);

    ResultDTO<String> login(String identifier, String credential, IdentityTypeEnum identityType);

    UserDO getUser(String token);

    String grantTicket(String token);

    void logoutByToken(String logoutAppName, String token);
}
