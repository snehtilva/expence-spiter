package com.example.expenseapp.models;

public class Balance {
    private String memberName;
    private double balance;

    public Balance(String memberName, double balance) {
        this.memberName = memberName;
        this.balance = balance;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}