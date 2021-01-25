package com.neatfox.mishutt.ui.model;

import java.util.List;

public class LoanType {

    private String type;

    private List<Loan> loan;

    public LoanType(String type, List<Loan> loan) {
        this.type = type;
        this.loan = loan;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Loan> getLoan() {
        return loan;
    }

    public void setLoan(List<Loan> loan) {
        this.loan = loan;
    }
}
