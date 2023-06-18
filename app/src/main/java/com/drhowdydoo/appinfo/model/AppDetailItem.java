package com.drhowdydoo.appinfo.model;

public class AppDetailItem {

    private int icon;
    private String title;
    private StringCount value;
    private boolean isExpanded;

    public AppDetailItem(int icon, String title, StringCount value) {
        this.icon = icon;
        this.title = title;
        this.value = value;
        isExpanded = false;
    }


    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        if (getValue().getCount() == 0) return title;
        return title + " (" + getValue().getCount() + ")";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public StringCount getValue() {
        return value;
    }

    public void setValue(StringCount value) {
        this.value = value;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
