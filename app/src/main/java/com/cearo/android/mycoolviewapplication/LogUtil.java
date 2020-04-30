package com.cearo.android.mycoolviewapplication;

public class LogUtil {

    private String tag;

    public LogUtil(String tag) {
        this.tag = tag;
    }

    public void print(String s) {
        System.out.println(tag + ":" + s);
    }
}
