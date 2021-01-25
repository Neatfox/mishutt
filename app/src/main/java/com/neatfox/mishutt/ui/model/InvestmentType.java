package com.neatfox.mishutt.ui.model;

import java.util.List;

public class InvestmentType {

    private String type;

    private List<Investment> investment;

    public InvestmentType(String type, List<Investment> investment) {
        this.type = type;
        this.investment = investment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Investment> getInvestment() {
        return investment;
    }

    public void setInvestment(List<Investment> investment) {
        this.investment = investment;
    }
}
