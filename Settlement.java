package com.example.expenseapp.models;

import com.google.gson.annotations.SerializedName;

public class Settlement {
    @SerializedName("id")
    private String id;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("from_member_id")
    private String fromMemberId;

    @SerializedName("to_member_id")
    private String toMemberId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("date")
    private String date;

    @SerializedName("status")
    private String status;

    @SerializedName("related_expense_id")
    private String relatedExpenseId;

    public Settlement() {
    }

    public Settlement(String groupId, String fromMemberId, String toMemberId, double amount, String status) {
        this.groupId = groupId;
        this.fromMemberId = fromMemberId;
        this.toMemberId = toMemberId;
        this.amount = amount;
        this.status = status;
    }

    public Settlement(String id, String groupId, String fromMemberId, String toMemberId,
                      double amount, String date, String status) {
        this.id = id;
        this.groupId = groupId;
        this.fromMemberId = fromMemberId;
        this.toMemberId = toMemberId;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(String fromMemberId) { this.fromMemberId = fromMemberId; }

    public String getToMemberId() { return toMemberId; }
    public void setToMemberId(String toMemberId) { this.toMemberId = toMemberId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRelatedExpenseId() { return relatedExpenseId; }
    public void setRelatedExpenseId(String relatedExpenseId) { this.relatedExpenseId = relatedExpenseId; }
}