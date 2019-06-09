package com.mad.mylibrary;


import java.util.HashMap;

public final class SharedClass {
    /**
     * Key for onSaveInstanceState() and onRestoreInstanceState()
     */
    public static final String Name = "keyName";
    public static final String Password = "keyPassword";
    public static final String Description = "keyDescription";
    public static final String Address = "keyAddress";
    public static final String Mail = "keyMail";
    public static final String Price = "keyEuroPrice";
    public static final String Photo = "keyPhoto";
    public static final String Phone = "keyPhone";
    public static final String Time = "keyTime";
    public static final String Quantity = "keyQuantity";
    public static final String CameraOpen = "keyCameraDialog";
    public static final String PriceOpen = "keyPriceDialog";
    public static final String QuantOpen = "keyQuantityDialog";
    public static final String TimeClose = "keyTimeClose";
    public static final String TimeOpen = "keyTimeOpen";

    /**
     * Status of an order
     */
    public static final int STATUS_UNKNOWN = 1000;
    public static final int STATUS_DELIVERING = 1001;
    public static final int STATUS_DELIVERED = 1002;
    public static final int STATUS_DISCARDED = 1003;

    /**
     * Useful values key to retrieve data from activity (Intent)
     */
    public static final String EDIT_EXISTING_DISH = "DISH_NAME";
    public static final String ORDER_ID = "ORDER_ID";
    public static final String CUSTOMER_ID = "CUSTOMER_ID";

    /**
     * Permission values
     */
    public static final int PERMISSION_GALLERY_REQUEST = 1;
    public static final int GOOGLE_SIGIN = 101;
    public static final int SIGNUP = 102;

    /**
     * Firebase paths
     */
    public static String ROOT_UID = "";
    public static User user;
    public static final String RESTAURATEUR_INFO = "/restaurants";
    public static final String DISHES_PATH =  "/dishes";
    public static final String RESERVATION_PATH = "/reservation";
    public static final String ACCEPTED_ORDER_PATH = "/order";
    public static final String RESTAURATEUR_REVIEW = "/reviews";
    public static final String RIDERS_PATH = "/riders";
    public static final String RIDERS_ORDER = "/pending";
    public static final String CUSTOMER_PATH = "/customers";
    public static final String CUSTOMER_FAVOURITE_RESTAURANT_PATH = "/favourites";

    /**
     * List of orders for a customer
     */
    public static HashMap<String, Integer> orderToTrack = new HashMap<String, Integer>();
}