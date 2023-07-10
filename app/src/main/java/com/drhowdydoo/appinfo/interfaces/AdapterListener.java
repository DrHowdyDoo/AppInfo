package com.drhowdydoo.appinfo.interfaces;

public interface AdapterListener {

    boolean isContextualBarShown();

    void onItemLongClicked();

    void removeContextualBar();

    void setContextualBarTitle(int count);

    void allItemSelected();

}
