package com.gulimall.common.constant;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-11-21:12
 */
public class ProductConstant {

    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"base"),ATTR_TYPE_SALE(0,"sale");
        private int code;
        private String msg;

        AttrEnum(int code, String msg) {
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
}
