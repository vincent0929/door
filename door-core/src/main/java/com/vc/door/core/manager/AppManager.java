package com.vc.door.core.manager;

import com.vc.door.core.dao.AppDAO;
import com.vc.door.core.entity.AppDO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class AppManager {

    @Autowired
    private AppDAO appDAO;

    public void add(AppDO appDO) {
        if (appDO != null) {
            appDAO.add(appDO);
        }
    }

    //@Cacheable(cacheNames = "app:getByName", key = "'app:'.concat(#name)")
    public AppDO getByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return appDAO.getByName(name);
    }
}
