package com.wang.getapk.model;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/25
 */

public class StickyPinyin extends App {

    public StickyPinyin(String pinyin) {
        if (pinyin != null && pinyin.matches("^[a-zA-Z]$")) {
            this.namePinyin = pinyin.toUpperCase();
        } else {
            namePinyin = "#";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof App) {
            return namePinyin.equals(((App) obj).namePinyin);
        }
        return super.equals(obj);
    }
}
