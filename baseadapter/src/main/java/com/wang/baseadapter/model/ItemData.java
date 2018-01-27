package com.wang.baseadapter.model;


/**
 * 列表数据类
 */
public class ItemData {
    private int mDataType;
    private Object mData;

    public ItemData(int dataType, Object data) {
        this.mDataType = dataType;
        this.mData = data;
    }

    public ItemData(int dataType) {
        this(dataType, null);
    }

    public int getDataType() {
        return mDataType;
    }

    public void setDataType(int dataType) {
        this.mDataType = dataType;
    }

    @SuppressWarnings("unchecked")
    public<T> T getData() {
        return (T) mData;
    }

    public void setData(Object data) {
        this.mData = data;
    }

    @Override
    public boolean equals(Object o) {
        return mData != null && mData.equals(((ItemData) o).getData()) || super.equals(o);
    }
}
