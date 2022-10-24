package com.example.itis5280_project8.apicalls;

import java.io.Serializable;
import java.util.Objects;

public class Item implements Serializable {
    String name, photo;
    Double price;
    Integer discount;
    Integer quantity = 1;

    public Item() {
        // empty
    }

    public Item(String name, String photo, Double price, Integer discount) {
        this.name = name;
        this.photo = photo;
        this.price = price;
        this.discount = discount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getDiscountedPrice() {
        double discountedPrice = price - (price * ((double)discount / 100));
        return Math.round(discountedPrice * 100.0) / 100.0;
    }

    public Double getTotalCost() {
        double totalCost = getDiscountedPrice() * quantity;
        return Math.round(totalCost * 100.0) / 100.0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(name, item.name) && Objects.equals(photo, item.photo) && Objects.equals(price, item.price) && Objects.equals(discount, item.discount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, photo, price, discount);
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", photo='" + photo + '\'' +
                ", price=" + price +
                ", discount=" + discount +
                ", quantity=" + quantity +
                '}';
    }
}
