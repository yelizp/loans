package com.example.model;

public class Loan {
    private long LoanId;
    private long AccountId;
    private double Amount;

    public Loan(long loanId, long accountId, double amount) {
        LoanId = loanId;
        AccountId = accountId;
        Amount = amount;
    }

    public long getLoanId() {
        return LoanId;
    }

    public long getAccountId() {
        return AccountId;
    }

    public double getAmount() {
        return Amount;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "LoanId=" + LoanId +
                ", AccountId=" + AccountId +
                ", Amount=" + Amount +
                '}';
    }
}
