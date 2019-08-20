package com.vc.door.core.dao;

import com.vc.door.core.entity.LoginLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LoginLogDAO {

    Long add(LoginLogDO loginLogDO);
}
