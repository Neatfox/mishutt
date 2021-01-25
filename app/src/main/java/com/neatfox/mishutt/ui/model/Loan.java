package com.neatfox.mishutt.ui.model;

public class Loan {

    private String id,bank_name,loan_type,min_interest_rate,max_interest_rate,processing_fee,
            min_loan_amount,max_loan_amount,min_tenure,max_tenure;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getLoan_type() {
        return loan_type;
    }

    public void setLoan_type(String loan_type) {
        this.loan_type = loan_type;
    }

    public String getMin_interest_rate() {
        return min_interest_rate;
    }

    public void setMin_interest_rate(String min_interest_rate) {
        this.min_interest_rate = min_interest_rate;
    }

    public String getMax_interest_rate() {
        return max_interest_rate;
    }

    public void setMax_interest_rate(String max_interest_rate) {
        this.max_interest_rate = max_interest_rate;
    }

    public String getProcessing_fee() {
        return processing_fee;
    }

    public void setProcessing_fee(String processing_fee) {
        this.processing_fee = processing_fee;
    }

    public String getMin_loan_amount() {
        return min_loan_amount;
    }

    public void setMin_loan_amount(String min_loan_amount) {
        this.min_loan_amount = min_loan_amount;
    }

    public String getMax_loan_amount() {
        return max_loan_amount;
    }

    public void setMax_loan_amount(String max_loan_amount) {
        this.max_loan_amount = max_loan_amount;
    }

    public String getMin_tenure() {
        return min_tenure;
    }

    public void setMin_tenure(String min_tenure) {
        this.min_tenure = min_tenure;
    }

    public String getMax_tenure() {
        return max_tenure;
    }

    public void setMax_tenure(String max_tenure) {
        this.max_tenure = max_tenure;
    }
}
