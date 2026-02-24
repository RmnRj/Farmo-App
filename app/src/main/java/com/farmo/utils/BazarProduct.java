package com.farmo.utils;

public class BazarProduct {

    private int    id;
    private String name;
    private double price;
    private int    discount;      // percentage, e.g. 15 means "15%"
    private double rating;
    private int    reviewCount;
    private String imageUrl;

    // ── Getters ──────────────────────────────────────────────────────────────────

    public int    getId()          { return id; }
    public String getName()        { return name; }
    public double getPrice()       { return price; }
    public int    getDiscount()    { return discount; }
    public double getRating()      { return rating; }
    public int    getReviewCount() { return reviewCount; }
    public String getImageUrl()    { return imageUrl; }

    private boolean isConnected; // Add this field

    // ── Setters ──────────────────────────────────────────────────────────────────

    public void setId(int id)                  { this.id = id; }
    public void setName(String name)           { this.name = name; }
    public void setPrice(double price)         { this.price = price; }
    public void setDiscount(int discount)      { this.discount = discount; }
    public void setRating(double rating)       { this.rating = rating; }
    public void setReviewCount(int reviewCount){ this.reviewCount = reviewCount; }
    public void setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }

    // ── Convenience ──────────────────────────────────────────────────────────────

    /** Returns a formatted price string, e.g. "Rs. 150" */
    public String getFormattedPrice() {
        return "Rs. " + (int) price;
    }

    /** Returns a formatted discount string, e.g. "-15%" (empty string if 0) */
    public String getFormattedDiscount() {
        return discount > 0 ? "-" + discount + "%" : "";
    }

    public boolean isConnected() {
        return isConnected;
    }

    // Add this setter method (needed for the JSON parser)
    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}

