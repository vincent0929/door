package com.vc.door.core.manager;

import com.vc.door.core.constant.IdentityTypeEnum;
import com.vc.door.core.dao.AccountDAO;
import com.vc.door.core.entity.AccountDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountManager {

    @Autowired
    private AccountDAO accountDAO;

    public AccountDO getByIdentifier(String identifier, String credential, IdentityTypeEnum identityType) {
        if (identityType == null) {
            return null;
        }
        return accountDAO.getByIdentifier(identifier, credential, identityType.getCode());
    }
}
