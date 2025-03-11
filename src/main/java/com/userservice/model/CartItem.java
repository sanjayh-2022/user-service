package com.userservice.model;

import javax.persistence.Id;

public class CartItem {

    @Id 
    private String id;
    private String productName;
    private int quantity = 0;
    private double price;
    private String image;

    public CartItem() {}

    public CartItem(String productName, double price, String image) {
        this.productName = productName;
        this.price = price;
        this.image=image;
    }
    public String getProductName() {
        return productName;
    }

    public String getId() {
        return id;
    }
    
    public String getImage() {
        return image;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public void incrementQuantity() {
        this.quantity += 1;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", image='" + image + '\'' +
                '}';
    }
}