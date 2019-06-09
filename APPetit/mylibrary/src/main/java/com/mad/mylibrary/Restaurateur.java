package com.mad.mylibrary;

import java.io.Serializable;

public final class Restaurateur implements Serializable {
    private String mail, name, addr, cuisine, openingTime, phone, photoUri ;

    public Restaurateur() {
        this.mail = "";
        this.name = "";
        this.addr = "";
        this.cuisine = "";
        this.phone = "";
        this.photoUri = null;
    }

    public Restaurateur(String mail, String name, String addr, String cuisine, String openingTime, String phone, String photoUri) {
        this.mail = mail;
        this.name = name;
        this.addr = addr;
        this.cuisine = cuisine;
        this.openingTime = openingTime;
        this.phone = phone;
        this.photoUri = photoUri;
    }

    public String getMail() {
        return mail;
    }

    public String getName() {
        return name;
    }

    public String getAddr() {
        return addr;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhotoUri() {
        return photoUri;
    }
}