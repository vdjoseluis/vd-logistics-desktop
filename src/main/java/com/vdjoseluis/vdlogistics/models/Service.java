package com.vdjoseluis.vdlogistics.models;

import java.util.Date;

public class Service {

    private String id;
    private Date date;
    private String operator;
    private String operatorId;
    private String type;
    private String customer;
    private String customerId;
    private String status;
    private String city;
    private String description;
    private String comments;

    public Service() {
    }

    public Service(String id, Date date, String type, String operator, String customer, String city) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.operator = operator;
        this.customer = customer;
        this.city = city;
    }

    public Service(String id, Date date, String operator, String type, String customer, String status, String description, String comments) {
        this.id = id;
        this.date = date;
        this.operator = operator;
        this.type = type;
        this.customer = customer;
        this.status = status;
        this.description = description;
        this.comments = comments;
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
    
    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatoIdr(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }
    
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
