package com.andlp.back;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.andlp.back.bean.Bean_Activity;

/**
 * 717219917@qq.com      2017/11/27  14:44
 */

public class LifeCycleCallback_Activity implements Application.ActivityLifecycleCallbacks {
    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.i("app","进入activity*****************************");
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onCreated();");
        Log.i("app","进入"+activity.getClass().getName()+".onCreated();");
        Log.i("app","进入"+activity.getClass().getCanonicalName()+".onCreated();");
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
        SwipeBack.convertActivityToTranslucent(activity);//activity背景设为透明
        activity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        activity.getWindow().getDecorView().setBackgroundDrawable(null);
        SwipeBackLayout   mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(activity).inflate( R.layout.swipeback_layout, null);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_ALL );
        mSwipeBackLayout.attachToActivity(activity);//每次创建activity 就创建一个根布局
        SwipeBack.acts.add(new Bean_Activity(activity,mSwipeBackLayout));


        ((FragmentActivity)activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(new LifeCycleCallback_Fragment(),false);
//        ((FragmentActivity)activity).getSupportFragmentManager().getFragments().get(0);
    }
    @Override public void onActivityStarted(Activity activity) {
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onActivityStarted();");
    }
    @Override public void onActivityResumed(Activity activity) {
        Log.i("app","进入"+activity.getClass().getSimpleName()+".onActivityResumed();");

        for (Bean_Activity act:SwipeBack.acts){
            Log.i("app","----遍历时act名字"+act.getClass().getSimpleName());
            Log.i("app","----当前act名字"+activity.getClass().getSimpleName());
            if ( activity.getClass().getSimpleName().equals(act.getClass().getSimpleName())){
                act.isOnResume=true;//设置可见
            }

        }


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
