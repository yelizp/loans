package com.example.model;

public class LoanAgg {
    private int AccountType;
    private int TotalCount;
    private double TotalAmount;
    private int LastMinuteCount;

    public LoanAgg() {
    }

    public LoanAgg(int accountType, int totalCount, double totalAmount, int lastMinuteCount) {
        AccountType = accountType;
        TotalCount = totalCount;
        TotalAmount = totalAmount;
        LastMinuteCount = lastMinuteCount;
    }

    public int getAccountType() {
        return AccountType;
    }

    public void setAccountType(int accountType) {
        AccountType = accountType;
    }

    public int getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(int totalCount) {
        TotalCount = totalCount;
    }

    public double getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        TotalAmount = totalAmount;
    }

    public int getLastMinuteCount() {
        return LastMinuteCount;
    }

    public void setLastMinuteCount(int lastMinuteCount) {
        LastMinuteCount = lastMinuteCount;
    }

    @Override
    public String toString() {
        return "LoanAgg{" +
                "AccountType=" + AccountType +
                ", TotalCount=" + TotalCount +
                ", TotalAmount=" + TotalAmount +
                ", LastMinuteCount=" + LastMinuteCount +
                '}';
    }
}
