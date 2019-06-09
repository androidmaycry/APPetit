package com.mad.mylibrary;

public class ReviewItem {
    private int stars;
    private String comment;
    private String user_key;
    private String img;
    private String name;

    public ReviewItem() {
    }

    public ReviewItem(int stars, String comment, String user_key, String img, String name) {
        this.stars = stars;
        this.comment = comment;
        this.user_key = user_key;
        this.img = img;
        this.name = name;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
