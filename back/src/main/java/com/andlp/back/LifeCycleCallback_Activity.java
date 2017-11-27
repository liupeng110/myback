package com.andlp.back;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 717219917@qq.com      2017/11/27  14:44
 */

public class LifeCycleCallback_Activity implements Application.ActivityLifecycleCallbacks {
    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.i("app","进入activity*****************************");
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onCreated();");
//        MyApp.list.add(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0沉浸状态栏
            Window window = activity.getWindow();//透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);//设置状态栏的颜色
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        ((FragmentActivity)activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(new LifeCycleCallback_Fragment(),false);
//        ((FragmentActivity)activity).getSupportFragmentManager().getFragments().get(0);
    }
    @Override public void onActivityStarted(Activity activity) {
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onActivityStarted();");
    }
    @Override public void onActivityResumed(Activity activity) {
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onActivityResumed();");
    }
    @Override  public void onActivityPaused(Activity activity) {
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onActivityPaused();");
    }
    @Override public void onActivityStopped(Activity activity) {
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onActivityStopped();");
    }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onActivitySaveInstanceState();");
    }
    @Override public void onActivityDestroyed(Activity activity) {
//        MyApp.list.remove(activity);
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onActivityDestroyed();");
        Log.i("app","进入activity///////////////////////////////////////////////");

    }
}
