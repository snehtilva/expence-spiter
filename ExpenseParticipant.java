package com.example.expenseapp.models;

import com.google.gson.annotations.SerializedName;

public class ExpenseParticipant {
    @SerializedName("expense_id")
    private String expenseId;

    @SerializedName("member_id")
    private String memberId;

    public ExpenseParticipant() {
    }

    public ExpenseParticipant(String expenseId, String memberId) {
        this.expenseId = expenseId;
        this.memberId = memberId;
    }

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
}