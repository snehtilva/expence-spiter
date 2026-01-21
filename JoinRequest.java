package com.example.expenseapp.models;

import com.google.gson.annotations.SerializedName;

public class JoinRequest {
    @SerializedName("id")
    private String id;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("requester_name")
    private String requesterName;

    @SerializedName("requester_email")
    private String requesterEmail;

    @SerializedName("status")
    private String status; // "pending", "approved", "rejected"

    @SerializedName("created_date")
    private String createdDate;

    // Empty constructor
    public JoinRequest() {
    }

    // Constructor for creating new join request
    public JoinRequest(String groupId, String requesterName, String requesterEmail) {
        this.groupId = groupId;
        this.requesterName = requesterName;
        this.requesterEmail = requesterEmail;
        this.status = "pending";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String requesterEmail) { this.requesterEmail = requesterEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}

// Location: app/src/main/java/com/example/expenseapp/models/JoinRequest.java