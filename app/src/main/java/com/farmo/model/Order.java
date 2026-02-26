package com.farmo.model;

public class Order {

    private String id;
    private String consumerName;   // "customerName" in JSON
    private String productName;    // "product" in JSON
    private int    quantity;
    private double totalAmount;    // "totalPrice" in JSON
    private String status;
    private String orderDate;      // "date" in JSON
    private String userId;
    private String shippingAddress;
    private String expectedDeliveryDate;

    // ── 10-arg constructor (matches FarmerOrderManagementActivity) ─────────
    public Order(String id,
                 String consumerName,
                 String productName,
                 int    quantity,
                 double totalAmount,
                 String status,
                 String orderDate,
                 String userId,
                 String shippingAddress,
                 String expectedDeliveryDate) {
        this.id                  = id;
        this.consumerName        = consumerName;
        this.productName         = productName;
        this.quantity            = quantity;
        this.totalAmount         = totalAmount;
        this.status              = status;
        this.orderDate           = orderDate;
        this.userId              = userId;
        this.shippingAddress     = shippingAddress;
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    // ── No-arg constructor ─────────────────────────────────────────────────
    public Order() {}

    // ── Getters ────────────────────────────────────────────────────────────
    public String getId()                   { return id; }
    public String getConsumerName()         { return consumerName; }
    public String getProductName()          { return productName; }
    public int    getQuantity()             { return quantity; }
    public double getTotalAmount()          { return totalAmount; }
    public String getStatus()              { return status; }
    public String getOrderDate()           { return orderDate; }
    public String getUserId()              { return userId; }
    public String getShippingAddress()     { return shippingAddress; }
    public String getExpectedDeliveryDate(){ return expectedDeliveryDate; }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setId(String id)                           { this.id = id; }
    public void setConsumerName(String consumerName)       { this.consumerName = consumerName; }
    public void setProductName(String productName)         { this.productName = productName; }
    public void setQuantity(int quantity)                  { this.quantity = quantity; }
    public void setTotalAmount(double totalAmount)         { this.totalAmount = totalAmount; }
    public void setStatus(String status)                   { this.status = status; }
    public void setOrderDate(String orderDate)             { this.orderDate = orderDate; }
    public void setUserId(String userId)                   { this.userId = userId; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setExpectedDeliveryDate(String date)       { this.expectedDeliveryDate = date; }
}