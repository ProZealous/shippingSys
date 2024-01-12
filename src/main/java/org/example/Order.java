//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example;

import java.util.Objects;

class Order {
    String orderNumber;
    String totalPrice;
    String firstName;
    String lastName;
    String street1;
    String street2;
    String zipcode;
    String city;
    String phone;
    String email;
    String country;
    String weight;
    String fulfilled;
    String cancelled;
    String shippingID;
    String shippingUrl;
    String orderId;
    String trackingCompany;

    public String toKg(String weight) {
        double weightNum = (double)Integer.parseInt(weight);
        weightNum /= 1000.0;
        return String.valueOf(weightNum);
    }

    public Order(String orderId, String orderNumber, String totalPrice, String firstName, String lastName, String street1, String street2, String zipcode, String city, String phone, String email, String country, String weight, String fulfilled, String cancelled, String shippingID, String shippingUrl, String trackingCompany) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.firstName = firstName;
        this.lastName = lastName;
        this.street1 = street1;
        this.street2 = street2;
        this.zipcode = zipcode;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.country = country;
        this.weight = this.toKg(weight);
        this.fulfilled = fulfilled;
        this.cancelled = cancelled;
        this.shippingUrl = shippingUrl;
        this.shippingID = shippingID;
        this.trackingCompany = trackingCompany;
    }

    public String getTrackingCompany() {
        return this.trackingCompany;
    }

    public void setShippingID(String shippingID) {
        this.shippingID = shippingID;
    }

    public void setShippingUrl(String shippingUrl) {
        this.shippingUrl = shippingUrl;
    }

    public String getShippingUrl() {
        return this.shippingUrl;
    }

    public String getShippingID() {
        return this.shippingID;
    }

    public String getCancelled() {
        return this.cancelled;
    }

    public String getFulfilled() {
        return this.fulfilled;
    }

    public String getWeight() {
        return this.weight;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public String getTotalPrice() {
        return this.totalPrice;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getStreet1() {
        return this.street1;
    }

    public String getStreet2() {
        return this.street2;
    }

    public String getZipcode() {
        return this.zipcode;
    }

    public String getCity() {
        return this.city;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getEmail() {
        return this.email;
    }

    public String getCountry() {
        return this.country;
    }

    public String toString() {
        if (!Objects.equals(this.getCancelled(), "null")) {
            return this.getOrderNumber() + " (Cancelled)";
        } else {
            return !Objects.equals(this.getFulfilled(), "null") ? this.getOrderNumber() + " (Shipped)" : this.getOrderNumber();
        }
    }
}
