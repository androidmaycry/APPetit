package com.mad.mylibrary;

public class ReviewRestaurantItem {
    private String comment;
    private int rating;

    public ReviewRestaurantItem(String comment, int rating) {
        this.comment = comment;
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }
}
