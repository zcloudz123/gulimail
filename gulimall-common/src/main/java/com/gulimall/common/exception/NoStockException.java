package com.gulimall.common.exception;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-24-16:35
 */
public class NoStockException extends RuntimeException {
    static final long serialVersionUID = 1L;

    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id:" + skuId + "；没有足够的库存了");
        this.skuId = skuId;
    }

    public Long getSkuId() {
        return skuId;
    }
}
