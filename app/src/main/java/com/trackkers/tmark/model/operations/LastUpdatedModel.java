package com.trackkers.tmark.model.operations;

public class LastUpdatedModel {

    String t,lu;

    public LastUpdatedModel(String t, String lu) {
        this.t = t;
        this.lu = lu;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getLu() {
        return lu;
    }

    public void setLu(String lu) {
        this.lu = lu;
    }
}
