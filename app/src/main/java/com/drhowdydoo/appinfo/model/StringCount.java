package com.drhowdydoo.appinfo.model;

public class StringCount {

    private String text;
    private int count;

    public StringCount(String text, int count) {
        this.text = text;
        this.count = count;
    }

    public StringCount(String text) {
        this.text = text;
        this.count = 0;
    }

    public String getText() {
        if (text == null) return "";
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isValueEmpty() {
        return text == null || text.isEmpty();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
