package com.neatfox.mishutt.ui.model;

public class Wallet {

    private String prime_transaction_id,transaction_date,transaction_time,transaction_amount,
            service_name,service_type,commission_amount,customer_params;

    public String getPrime_transaction_id() {
        return prime_transaction_id;
    }

    public void setPrime_transaction_id(String prime_transaction_id) {
        this.prime_transaction_id = prime_transaction_id;
    }

    public String getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date = transaction_date;
    }

    public String getTransaction_time() {
        return transaction_time;
    }

    public void setTransaction_time(String transaction_time) {
        this.transaction_time = transaction_time;
    }

    public String getTransaction_amount() {
        return transaction_amount;
    }

    public void setTransaction_amount(String transaction_amount) {
        this.transaction_amount = transaction_amount;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    public String getCommission_amount() {
        return commission_amount;
    }

    public void setCommission_amount(String commission_amount) {
        this.commission_amount = commission_amount;
    }

    public String getCustomer_params() {
        return customer_params;
    }

    public void setCustomer_params(String customer_params) {
        this.customer_params = customer_params;
    }
}
