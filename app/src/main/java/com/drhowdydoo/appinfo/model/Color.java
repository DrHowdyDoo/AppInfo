package com.drhowdydoo.appinfo.model;

public class Color {

    private int colorPrimary;
    private int colorSecondary;

    public Color(int colorPrimary, int colorSecondary) {
        this.colorPrimary = colorPrimary;
        this.colorSecondary = colorSecondary;
    }

    public int getColorPrimary() {
        return colorPrimary;
    }

    public void setColorPrimary(int colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public int getColorSecondary() {
        return colorSecondary;
    }

    public void setColorSecondary(int colorSecondary) {
        this.colorSecondary = colorSecondary;
    }


}
