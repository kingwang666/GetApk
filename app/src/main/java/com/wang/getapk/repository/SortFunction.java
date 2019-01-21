package com.wang.getapk.repository;

import com.wang.baseadapter.model.ItemArray;
import com.wang.baseadapter.model.ItemData;
import com.wang.getapk.model.App;
import com.wang.getapk.model.StickyPinyin;
import com.wang.getapk.model.StickyTime;
import com.wang.getapk.util.PinyinComparator;
import com.wang.getapk.util.TimeComparator;
import com.wang.getapk.view.adapter.AppAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import io.reactivex.functions.Function;

/**
 * Author wang
 * Date: 2018/1/27.
 */

public class SortFunction implements Function<List<App>, ItemArray> {

    private final DateFormat mDateFormat;
    private final boolean mSortByTime;

    public SortFunction(DateFormat dateFormat, boolean sortByTime) {
        mDateFormat = dateFormat;
        mSortByTime = sortByTime;
    }

    @Override
    public ItemArray apply(List<App> apps) throws Exception {
        ItemArray itemArray = new ItemArray();
        List<Object> stickies = new ArrayList<>();
        for (App app : apps) {
            if (mSortByTime) {
                StickyTime sticky = new StickyTime(app.time.substring(0, 7));
                if (!stickies.contains(sticky)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mDateFormat.parse(app.time));
                    calendar.set(Calendar.DAY_OF_MONTH, 0);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.add(Calendar.MONTH, 1);
                    sticky.lastUpdateTime = calendar.getTimeInMillis() + 24 * 60 * 60 * 1000 - 1;
                    stickies.add(sticky);
                    itemArray.add(new ItemData(AppAdapter.TYPE_STICKY, sticky));
                }
            } else {
                StickyPinyin sticky = new StickyPinyin(app.namePinyin.isEmpty() ? null : app.namePinyin.substring(0, 1));
                if (!stickies.contains(sticky)) {
                    stickies.add(sticky);
                    itemArray.add(new ItemData(AppAdapter.TYPE_STICKY, sticky));
                }
            }
            itemArray.add(new ItemData(AppAdapter.TYPE_APP, app));
        }
        if (mSortByTime) {
            Collections.sort(itemArray, new TimeComparator());
        } else {
            Collections.sort(itemArray, new PinyinComparator());
        }
        return itemArray;
    }

}
