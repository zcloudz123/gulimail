package com.gulimall.common.exception;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-10-18:34
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000,"系统位置异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败");

    private int code;
    private String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
