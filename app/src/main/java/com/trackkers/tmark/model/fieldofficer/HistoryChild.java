package com.trackkers.tmark.model.fieldofficer;

import android.os.Parcel;
import android.os.Parcelable;

public class HistoryChild implements Parcelable {

    private String name;
    private String dateTime;
    private String address;

    public HistoryChild(String name, String dateTime, String address) {
        this.name = name;
        this.dateTime = dateTime;
        this.address = address;
    }

    private HistoryChild(Parcel in) {
        name = in.readString();
        dateTime = in.readString();
        address = in.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(dateTime);
        dest.writeString(address);
    }


    public static final Creator<HistoryChild> CREATOR = new Creator<HistoryChild>() {
        @Override
        public HistoryChild createFromParcel(Parcel in) {
            return new HistoryChild(in);
        }

        @Override
        public HistoryChild[] newArray(int size) {
            return new HistoryChild[size];
        }
    };

}
