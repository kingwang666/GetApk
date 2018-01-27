package com.wang.getapk.util;

import com.wang.baseadapter.model.ItemData;
import com.wang.getapk.model.App;

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/26
 */

public class PinyinComparator implements Comparator<ItemData> {

    private final Pattern mPattern = Pattern.compile("^[a-zA-Z]");

    @Override
    public int compare(ItemData data1, ItemData data2) {
        String pinyin1 = ((App) data1.getData()).namePinyin;
        String pinyin2 = ((App) data2.getData()).namePinyin;
        if (pinyin1.equals("#") && isA2z(pinyin2)) {
            return 1;
        } else if (pinyin1.equals("#") && !isA2z(pinyin2)) {
            return -1;
        } else if (isA2z(pinyin1) && pinyin2.equals("#")) {
            return -1;
        } else if (!isA2z(pinyin1) && pinyin2.equals("#")) {
            return 1;
        } else if (isA2z(pinyin1) && !isA2z(pinyin2)) {
            return -1;
        } else if (!isA2z(pinyin1) && isA2z(pinyin2)) {
            return 1;
        } else {
            return pinyin1.compareToIgnoreCase(pinyin2);
        }
    }

    private boolean isA2z(String input) {
        return mPattern.matcher(input).find();
    }
}
