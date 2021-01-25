package com.neatfox.mishutt.ui.model;

import androidx.annotation.NonNull;

public class Spinner {

    private String type;

    public Spinner(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return type;
    }
}
