package com.vc.door.core.dao;

import com.vc.door.core.entity.AppDO;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface AppDAO {

    Long add(AppDO appDO);

    AppDO getByName(@Param("name") String name);
}
