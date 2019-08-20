package com.vc.door.core.constant;

import io.github.vincent0929.common.constant.BaseEnum;
import lombok.Getter;

@Getter
public enum AppStatusEnum implements BaseEnum<Integer> {

    ENABLE(1, "启用"), UNABLE(2, "禁用");

    private Integer code;

    private String desc;

    AppStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
