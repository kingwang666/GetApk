package com.wang.getapk.model;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/25
 */

public class StickyTime extends App {

    public StickyTime(String time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof App) {
            return time.equals(((App) obj).time);
        }
        return super.equals(obj);
    }
}
