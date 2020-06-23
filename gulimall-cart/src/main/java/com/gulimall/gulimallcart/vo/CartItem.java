package com.gulimall.gulimallcart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @decription:购物项
 * @author: zyy
 * @date 2020-06-22-9:13
 */
public class CartItem {
    private Long skuId;
    private Boolean check = true;
    private String title;
    private String img;
    private List<String> skuAttr;
    private BigDecimal price = new BigDecimal(0);
    private Integer count;
    private BigDecimal totalPrice = new BigDecimal(0);

    public Long getSkuId() {
        return skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public String getTitle() {
        return title;
    }

    public String getImg() {
        return img;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getCount() {
        return count;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotalPrice(){
        return price.multiply(new BigDecimal(count));
    }
}
