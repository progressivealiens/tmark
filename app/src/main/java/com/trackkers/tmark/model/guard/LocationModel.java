package com.trackkers.tmark.model.guard;

public class LocationModel {

    private double La, Lo;
    private String I, N, R,D;

    public LocationModel(double la, double lo, String i, String n, String r, String d) {
        La = la;
        Lo = lo;
        I = i;
        N = n;
        R = r;
        D = d;
    }

    public double getLa() {
        return La;
    }

    public void setLa(double la) {
        La = la;
    }

    public double getLo() {
        return Lo;
    }

    public void setLo(double lo) {
        Lo = lo;
    }

    public String getI() {
        return I;
    }

    public void setI(String i) {
        I = i;
    }

    public String getN() {
        return N;
    }

    public void setN(String n) {
        N = n;
    }

    public String getR() {
        return R;
    }

    public void setR(String r) {
        R = r;
    }

    public String getD() {
        return D;
    }

    public void setD(String d) {
        D = d;
    }
}
