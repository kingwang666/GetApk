package com.wang.getapk.util;

import com.wang.baseadapter.model.ItemData;
import com.wang.getapk.model.App;

import java.util.Comparator;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/26
 */

public class TimeComparator implements Comparator<ItemData> {
    @Override
    public int compare(ItemData data1, ItemData data2) {
        long time1 = ((App) data1.getData()).lastUpdateTime;
        long time2 = ((App) data2.getData()).lastUpdateTime;
        return Long.compare(time2, time1);
    }
}
