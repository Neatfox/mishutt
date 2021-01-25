package com.neatfox.mishutt.ui.model;

public class TransactionCategory {

    String transaction_category_id,category,frequency;

    public String getTransaction_category_id() {
        return transaction_category_id;
    }

    public void setTransaction_category_id(String transaction_category_id) {
        this.transaction_category_id = transaction_category_id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
