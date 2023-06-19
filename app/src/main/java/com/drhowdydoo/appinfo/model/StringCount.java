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
        if (text == null || text.isEmpty()) return "NONE";
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
