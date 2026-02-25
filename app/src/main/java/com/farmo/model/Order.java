package com.farmo.model;

public class Order {
    private String id;
    private String customerName;
    private String product;
    private int quantity;
    private double totalPrice;
    private String status;
    private String date;
    private String userId;
    private String shippingAddress;
    private String expectedDeliveryDate;

    public Order() {}

    public Order(String id, String customerName, String product, int quantity,
                 double totalPrice, String status, String date, String userId,
                 String shippingAddress, String expectedDeliveryDate) {
        this.id = id;
        this.customerName = customerName;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.date = date;
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getUserId() { return userId; }
    public String getShippingAddress() { return shippingAddress; }
    public String getExpectedDeliveryDate() { return expectedDeliveryDate; }

    public void setStatus(String status) { this.status = status; }
}