package com.example.expenseapp.models;

import com.google.gson.annotations.SerializedName;

public class Group {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private String code;

    @SerializedName("created_date")
    private String createdDate;

    // Empty constructor
    public Group() {
    }

    // Constructor for creating new group (without ID)
    public Group(String name, String code) {
        this.name = name;
        this.code = code;
    }

    // Full constructor with all fields
    public Group(String id, String name, String code, String createdDate) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.createdDate = createdDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}