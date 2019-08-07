package com.ghl.wuhan.tqpicturetest.app;

import android.app.Application;

/**
 * 项目名称：com.ghl.wuhan.tqpicturetest.app
 * 类描述：
 * 创建人：Liting
 * 创建时间：2019/8/1 15:14
 * 修改人：Liting
 * 修改时间：2019/8/1 15:14
 * 修改备注：
 * 版本：
 */

public class AppStr extends Application {
    private static boolean isCompleted;

    public static boolean isCompleted() {
        return isCompleted;
    }

    public static void setIsCompleted(boolean isCompleted) {
        AppStr.isCompleted = isCompleted;
    }
}
