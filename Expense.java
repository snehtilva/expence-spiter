package com.example.expenseapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Expense {
    @SerializedName("id")
    private String id;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("description")
    private String description;

    @SerializedName("amount")
    private double amount;

    @SerializedName("paid_by_member_id")
    private String paidByMemberId;

    @SerializedName("date")
    private String date;

    // transient means this won't be sent to Supabase
    private transient List<String> participants;

    public Expense() {
        this.participants = new ArrayList<>();
    }

    public Expense(String groupId, String description, double amount, String paidByMemberId, String date) {
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidByMemberId = paidByMemberId;
        this.date = date;
        this.participants = new ArrayList<>();
    }

    public Expense(String id, String groupId, String description, double amount,
                   String paidByMemberId, String date) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidByMemberId = paidByMemberId;
        this.date = date;
        this.participants = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaidByMemberId() { return paidByMemberId; }
    public void setPaidByMemberId(String paidByMemberId) { this.paidByMemberId = paidByMemberId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
}