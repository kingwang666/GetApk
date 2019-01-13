package com.wang.getapk.model;

/**
 * Author: wangxiaojie6
 * Date: 2019/1/2
 */
public class Sign {

    public String[] md5;
    public String[] sha1;
    public String[] sha256;

    public boolean hasHistory = false;

    public String[] historyMD5;
    public String[] historySHA1;
    public String[] historySHA256;

}
