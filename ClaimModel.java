package com.lostnfound.app;

public class ClaimModel {
    private String id, itemId, itemTitle, claimantId, claimantName, answerText, status;
    public ClaimModel() {}
    public ClaimModel(String id, String itemId, String itemTitle, String claimantId, String claimantName, String answerText, String status) {
        this.id = id; this.itemId = itemId; this.itemTitle = itemTitle; this.claimantId = claimantId; this.claimantName = claimantName; this.answerText = answerText; this.status = status;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public String getClaimantId() { return claimantId; }
    public void setClaimantId(String claimantId) { this.claimantId = claimantId; }
    public String getClaimantName() { return claimantName; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}