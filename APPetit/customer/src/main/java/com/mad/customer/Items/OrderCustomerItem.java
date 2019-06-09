package com.mad.customer.Items;

import java.io.Serializable;
import java.util.HashMap;

public class OrderCustomerItem implements Serializable {
    private String key, addrCustomer, totPrice;
    private HashMap<String, Integer> dishes; //key = dish name, value = quantity
    private Long time;
    private Long sort;
    private Integer status;
    private boolean rated;

    public OrderCustomerItem() {
    }

    public OrderCustomerItem(String key, String addrCustomer, String totPrice, HashMap<String, Integer> dishes, Long time, Long sort, Integer status, boolean rated) {
        this.key = key;
        this.addrCustomer = addrCustomer;
        this.totPrice = totPrice;
        this.dishes = dishes;
        this.time = time;
        this.sort = sort;
        this.status = status;
        this.rated = rated;
    }

    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAddrCustomer() {
        return addrCustomer;
    }

    public void setAddrCustomer(String addrCustomer) {
        this.addrCustomer = addrCustomer;
    }

    public String getTotPrice() {
        return totPrice;
    }

    public void setTotPrice(String totPrice) {
        this.totPrice = totPrice;
    }

    public HashMap<String, Integer> getDishes() {
        return dishes;
    }

    public void setDishes(HashMap<String, Integer> dishes) {
        this.dishes = dishes;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean isRated() {
        return rated;
    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }
}
