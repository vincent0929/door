package com.vc.door.core.entity;

import io.github.vincent0929.common.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

@EqualsAndHashCode(callSuper = true)
@Data
@Alias("loginLog")
public class LoginLogDO extends BaseDO {

    private Long userId;

    private Integer actionType;

    private Long appId;

    private String ip;

    private String info;
}
