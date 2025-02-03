package com.akstudios.cashmanager.Adaptor;

public class Transaction {
    private String type;
    private String category;
    private int amount;
    private String date;
    private long timestamp;

    public Transaction(String type, String category, int amount, String date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.timestamp = convertDateToTimestamp(date);
    }

    private long convertDateToTimestamp(String date) {
        try {
            java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(date).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public int getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Override the toString() method
    @Override
    public String toString() {
        return type + ": " + category + " - $" + amount + " on " + date;
    }
}
