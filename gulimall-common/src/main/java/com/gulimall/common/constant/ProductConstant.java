package com.gulimall.common.constant;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-11-21:12
 */
public class ProductConstant {

    public enum AttrTypeEnum {
        ATTR_TYPE_BASE(1,"base"),ATTR_TYPE_SALE(0,"sale");
        private int code;
        private String msg;

        AttrTypeEnum(int code, String msg) {
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
    
    public enum AttrSearchEnum {
        ATTR_SEARCH_INAVLIABLE(0),
        ATTR_SEARCH_AVIABLE(1);

        private int code;

        AttrSearchEnum(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public enum SpuStatusEnum {
        SPU_STATUS_NEW(0,"新建"),
        SPU_STATUS_UP(1,"上架"),
        SPU_STATUS_DOWN(2,"下架");
        private int code;
        private String msg;

        SpuStatusEnum(int code, String msg) {
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
