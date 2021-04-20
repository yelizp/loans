package com.example.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Loan {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    public String toJsonWithEventTime() {
        Gson gson = new GsonBuilder().create();
        String jsonStr = gson.toJson(this,this.getClass());
        JsonElement jsonElement = gson.toJsonTree(this);
        jsonElement.getAsJsonObject().addProperty("EventTime",
                simpleDateFormat.format(new Timestamp(System.currentTimeMillis())));
        jsonStr = gson.toJson(jsonElement);
        return jsonStr;
    }
}
