package com.camel.rabbitmq.bean;

import java.io.Serializable;

public class MyBean implements Serializable {
    private String usd;
    private String inr;

    public String getUsd() {
        return usd;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

    public String getInr() {
        return inr;
    }

    public void setInr(String inr) {
        this.inr = inr;
    }

    @Override
    public String toString() {
        return getInr() + getUsd();
    }
}
