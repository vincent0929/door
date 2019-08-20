package com.vc.door.core.entity;

import io.github.vincent0929.common.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

@EqualsAndHashCode(callSuper = true)
@Data
@Alias("user")
public class UserDO extends BaseDO {

    private String nick;

    private Integer status;
}
