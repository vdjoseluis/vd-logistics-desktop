package com.vdjoseluis.vdlogistics.models;

import java.util.Date;

public class Incident {

    private String id;
    private Date date;
    private String operator;
    private String description;
    private String service;
    private String status;

    public Incident() {
    }

    public Incident(String id, Date date, String operator, String description, String service, String status) {
        this.id = id;
        this.date = date;
        this.operator = operator;
        this.description = description;
        this.service = service;
        this.status = status;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
