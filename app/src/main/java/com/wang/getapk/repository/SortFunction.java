package com.wang.getapk.repository;

import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.functions.Function;


/**
 * Author wang
 * Date: 2018/1/27.
 */

public class SortFunction implements Function<List<App>, ItemArray> {

    private final boolean mSortByTime;

    public SortFunction(boolean sortByTime) {
        mSortByTime = sortByTime;
    }

    @Override
    public ItemArray apply(List<App> apps) throws Exception {
        ItemArray itemArray = new ItemArray();
        HashSet<String> stickies = new HashSet<>();
        for (App app : apps) {
            if (mSortByTime) {
                String stickyContent = app.time.substring(0, 7);
                if (!stickies.contains(stickyContent)) {
                    StickyTime sticky = new StickyTime(stickyContent);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(app.lastUpdateTime));
                    calendar.set(Calendar.DAY_OF_MONTH, 0);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.add(Calendar.MONTH, 1);
                    sticky.lastUpdateTime = calendar.getTimeInMillis() + 24 * 60 * 60 * 1000 - 1;
                    stickies.add(stickyContent);
                    itemArray.add(new ItemData(AppAdapter.TYPE_STICKY, sticky));
                }
            } else {
                String stickyContent = app.namePinyin.isEmpty() ? "" : app.namePinyin.substring(0, 1).toUpperCase();
                if (!stickies.contains(stickyContent)) {
                    StickyPinyin sticky = new StickyPinyin(stickyContent);
                    stickies.add(stickyContent);
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
