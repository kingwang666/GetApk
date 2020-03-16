package com.wang.getapk.util;

import java.io.Closeable;

/**
 * Created on 2020/3/16
 * Author: bigwang
 * Description:
 */
public class IOUtil {

    public static void closeQuiet(Closeable closeable){
        try {
            if (closeable != null){
                closeable.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
