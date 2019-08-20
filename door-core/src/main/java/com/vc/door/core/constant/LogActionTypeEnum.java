package com.vc.door.core.constant;

import io.github.vincent0929.common.constant.BaseEnum;
import lombok.Getter;

@Getter
public enum LogActionTypeEnum implements BaseEnum<Integer> {
    LOGIN(1, "登录"), VALID_TICKET(2, "验证Ticket"), LOGOUT(3, "注销");

    private Integer code;

    private String desc;

    LogActionTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
