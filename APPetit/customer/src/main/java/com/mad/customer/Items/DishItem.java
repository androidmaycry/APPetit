package com.mad.customer.Items;

public class DishItem {
    private String name;
    private String desc;
    private float price;
    private int quantity;
    private String photo;

    public DishItem() {
        this.name = "";
        this.desc = "";
        this.price = -1;
        this.quantity = -1;
        this.photo = null;
    }

    public DishItem(String name, String desc, float price, int quantity, String photo) {
        this.name = name;
        this.desc = desc;
        this.price = price;
        this.quantity = quantity;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPhotoUri() {
        return photo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPhotoUri(String photo) {
        this.photo = photo;
    }
}
