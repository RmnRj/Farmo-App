package com.farmo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MyProductsResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("products")
    private List<Product> products;

    @SerializedName("error")
    private String error;

    // ── Getters ───────────────────────────────────────────────────────────────

    public String       getStatus()   { return status; }
    public List<Product> getProducts() { return products; }
    public String       getError()    { return error; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setStatus(String status)             { this.status = status; }
    public void setProducts(List<Product> products)  { this.products = products; }
    public void setError(String error)               { this.error = error; }
}