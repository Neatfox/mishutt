package com.neatfox.mishutt.ui.model;

public class Investment {

    private String id,investment_type,scheme_name,min_invest_amount,max_invest_amount,aum,exit_load;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvestment_type() {
        return investment_type;
    }

    public void setInvestment_type(String investment_type) {
        this.investment_type = investment_type;
    }

    public String getScheme_name() {
        return scheme_name;
    }

    public void setScheme_name(String scheme_name) {
        this.scheme_name = scheme_name;
    }

    public String getMin_invest_amount() {
        return min_invest_amount;
    }

    public void setMin_invest_amount(String min_invest_amount) {
        this.min_invest_amount = min_invest_amount;
    }

    public String getMax_invest_amount() {
        return max_invest_amount;
    }

    public void setMax_invest_amount(String max_invest_amount) {
        this.max_invest_amount = max_invest_amount;
    }

    public String getAum() {
        return aum;
    }

    public void setAum(String aum) {
        this.aum = aum;
    }

    public String getExit_load() {
        return exit_load;
    }

    public void setExit_load(String exit_load) {
        this.exit_load = exit_load;
    }
}
