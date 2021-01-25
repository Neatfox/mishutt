package com.neatfox.mishutt.ui.model;

import java.util.List;

public class PortfolioCategory {

    private String category;

    private List<Portfolio> portfolio;

    public PortfolioCategory(String category, List<Portfolio> portfolio) {
        this.category = category;
        this.portfolio = portfolio;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Portfolio> getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(List<Portfolio> portfolio) {
        this.portfolio = portfolio;
    }
}
