package com.vdjoseluis.vdlogistics.models;

import java.util.Date;

public class Service {

    private String id;
    private Date date;
    private String type;
    private String operator;  
    private String customer; 
    private String city;     

    public Service() { }  
    public Service(String id, Date date, String type, String operator, String customer, String city) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.operator = operator;
        this.customer = customer;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
