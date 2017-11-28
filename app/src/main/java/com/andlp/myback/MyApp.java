package com.andlp.myback;

import android.app.Application;

import com.andlp.back.LifeCycleCallback_Activity;
import com.andlp.back.SwipeBack;

/**
 * 717219917@qq.com      2017/11/27  14:43
 */

public class MyApp extends Application{

    @Override  public void onCreate() {
        super.onCreate();
        CrashUtil.getInstance().init(this);
        SwipeBack.init(this);
    }
}
