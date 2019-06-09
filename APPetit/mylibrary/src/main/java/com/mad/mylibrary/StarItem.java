package com.mad.mylibrary;

public class StarItem {
    int tot_stars;
    int tot_review;
    int sort;

    public StarItem() {
    }

    public StarItem(int tot_stars, int tot_review, int sort) {
        this.tot_stars = tot_stars;
        this.tot_review = tot_review;
        this.sort = sort;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getTot_stars() {
        return tot_stars;
    }

    public void setTot_stars(int tot_stars) {
        this.tot_stars = tot_stars;
    }

    public int getTot_review() {
        return tot_review;
    }

    public void setTot_review(int tot_review) {
        this.tot_review = tot_review;
    }
}
