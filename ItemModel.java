package com.lostnfound.app;

import java.io.Serializable;

public class ItemModel implements Serializable {
    private String id;
    private String title;
    private String description;
    private String category;
    private String location;
    private String base64Image;
    private String status;
    private String postedByUid;
    private String postedByName;
    private String dateString;
    private String verificationQuestion;

    public ItemModel() {}

    public ItemModel(String id, String title, String description, String category, String location, String base64Image, String status, String postedByUid, String postedByName, String dateString, String verificationQuestion) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.base64Image = base64Image;
        this.status = status;
        this.postedByUid = postedByUid;
        this.postedByName = postedByName;
        this.dateString = dateString;
        this.verificationQuestion = verificationQuestion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getBase64Image() { return base64Image; }
    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPostedByUid() { return postedByUid; }
    public void setPostedByUid(String postedByUid) { this.postedByUid = postedByUid; }
    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }
    public String getDateString() { return dateString; }
    public void setDateString(String dateString) { this.dateString = dateString; }
    public String getVerificationQuestion() { return verificationQuestion; }
    public void setVerificationQuestion(String verificationQuestion) { this.verificationQuestion = verificationQuestion; }
}
