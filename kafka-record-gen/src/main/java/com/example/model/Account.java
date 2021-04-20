package com.example.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

public class Account {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
