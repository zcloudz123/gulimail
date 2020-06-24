package com.gulimall.gulimallcart.vo;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @decription:购物车
 * @author: zyy
 * @date 2020-06-22-9:13
 */
public class Cart {
    private List<CartItem> items;
    private Integer countNum;
    private Integer countType;

    private BigDecimal amount = new BigDecimal(0);
    private BigDecimal reduce = new BigDecimal(0);

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public Integer getCountNum() {
        int count = 0;
        if(CollectionUtils.isEmpty(items)){
            return 0;
        }else{
            for (CartItem cartItem :
                    items) {
                count += cartItem.getCount();
            }
            return count;
        }
    }

    public Integer getCountType() {
        if(items != null){
            return items.size();
        }
        return 0;
    }

    public BigDecimal getAmount() {
        BigDecimal total = new BigDecimal(0);
        if(CollectionUtils.isEmpty(items)){
            return total;
        }else{
            for (CartItem cartItem :
                    items) {
                if(cartItem.getCheck()){
                    total = total.add(cartItem.getTotalPrice());
                }
            }
            total = total.subtract(reduce);
            return total;
        }
    }
}
