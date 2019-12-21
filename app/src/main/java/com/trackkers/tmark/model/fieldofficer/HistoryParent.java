package com.trackkers.tmark.model.fieldofficer;

import android.os.Parcel;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class HistoryParent extends ExpandableGroup<HistoryChild> {
    private String siteName;
    private String routeName;

    public HistoryParent(String title, List<HistoryChild> items, String siteName, String routeName) {
        super(title, items);
        this.siteName = siteName;
        this.routeName = routeName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
}
