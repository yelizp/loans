package com.example.model;

public class Account {
    private long AccountId;
    private int AccountType;

    public Account(long accountId, int accountType) {
        AccountId = accountId;
        AccountType = accountType;
    }

    public long getAccountId() {
        return AccountId;
    }

    public int getAccountType() {
        return AccountType;
    }

    @Override
    public String toString() {
        return "Account{" +
                "AccountId=" + AccountId +
                ", AccountType=" + AccountType +
                '}';
    }
}
