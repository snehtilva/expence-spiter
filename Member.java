package com.example.expenseapp.models;

import com.google.gson.annotations.SerializedName;

public class Member {
    @SerializedName("id")
    private String id;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("joined_date")
    private String joinedDate;

    // For UI selection (not stored in database)
    private transient boolean isSelected;

    public Member() {
        this.isSelected = false;
    }

    // Constructor for creating new member without email (backward compatibility)
    public Member(String groupId, String name) {
        this.groupId = groupId;
        this.name = name;
        this.isSelected = false;
    }

    // Constructor for creating new member with email
    public Member(String groupId, String name, String email) {
        this.groupId = groupId;
        this.name = name;
        this.email = email;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getJoinedDate() { return joinedDate; }
    public void setJoinedDate(String joinedDate) { this.joinedDate = joinedDate; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}