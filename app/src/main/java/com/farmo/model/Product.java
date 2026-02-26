package com.farmo.model;

import com.example.farmo_test.model.Review;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {

    @SerializedName("product_id")        private String  productId;
    @SerializedName("product_name")      private String  productName;
    @SerializedName("category")          private String  category;
    @SerializedName("description")       private String  description;
    @SerializedName("cost_per_unit")     private double  costPerUnit;
    @SerializedName("unit")              private String  unit;
    @SerializedName("quantity_available")private double  quantityAvailable;
    @SerializedName("organic_status")    private String  organicStatus;
    @SerializedName("product_status")    private String  productStatus;
    @SerializedName("produced_date")     private String  producedDate;
    @SerializedName("delivery_options")  private String  deliveryOptions;
    @SerializedName("rating")            private double  rating;
    @SerializedName("rating_count")      private int     ratingCount;
    @SerializedName("sold_count")        private int     soldCount;
    @SerializedName("farmer_name")       private String  farmerName;
    @SerializedName("farmer_id")         private String  farmerId;
    @SerializedName("farmer_location")   private String  farmerLocation;
    @SerializedName("farmer_verified")   private boolean farmerVerified;
    @SerializedName("image")             private String  image;
    @SerializedName("reviews")           private List<Review> reviews;   // ← NEW

    // Getters
    public String       getProductId()          { return productId; }
    public String       getProductName()        { return productName; }
    public String       getCategory()           { return category; }
    public String       getDescription()        { return description; }
    public double       getCostPerUnit()        { return costPerUnit; }
    public String       getUnit()               { return unit; }
    public double       getQuantityAvailable()  { return quantityAvailable; }
    public String       getOrganicStatus()      { return organicStatus; }
    public String       getProductStatus()      { return productStatus; }
    public String       getProducedDate()       { return producedDate; }
    public String       getDeliveryOptions()    { return deliveryOptions; }
    public double       getRating()             { return rating; }
    public int          getRatingCount()        { return ratingCount; }
    public int          getSoldCount()          { return soldCount; }
    public String       getFarmerName()         { return farmerName; }
    public String       getFarmerId()           { return farmerId; }
    public String       getFarmerLocation()     { return farmerLocation; }
    public boolean      isFarmerVerified()      { return farmerVerified; }
    public String       getImage()              { return image; }
    public List<Review> getReviews()            { return reviews; }

    // Setters
    public void setProductId(String v)               { productId = v; }
    public void setProductName(String v)             { productName = v; }
    public void setCategory(String v)                { category = v; }
    public void setDescription(String v)             { description = v; }
    public void setCostPerUnit(double v)             { costPerUnit = v; }
    public void setUnit(String v)                    { unit = v; }
    public void setQuantityAvailable(double v)       { quantityAvailable = v; }
    public void setOrganicStatus(String v)           { organicStatus = v; }
    public void setProductStatus(String v)           { productStatus = v; }
    public void setProducedDate(String v)            { producedDate = v; }
    public void setDeliveryOptions(String v)         { deliveryOptions = v; }
    public void setRating(double v)                  { rating = v; }
    public void setRatingCount(int v)                { ratingCount = v; }
    public void setSoldCount(int v)                  { soldCount = v; }
    public void setFarmerName(String v)              { farmerName = v; }
    public void setFarmerId(String v)                { farmerId = v; }
    public void setFarmerLocation(String v)          { farmerLocation = v; }
    public void setFarmerVerified(boolean v)         { farmerVerified = v; }
    public void setImage(String v)                   { image = v; }
    public void setReviews(List<Review> v)           { reviews = v; }
}