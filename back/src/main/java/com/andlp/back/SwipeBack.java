package com.andlp.back;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Application;
import android.content.ComponentName;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andlp.back.bean.Bean_Activity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static android.content.Context.ACTIVITY_SERVICE;


/**
 * 717219917@qq.com      2017/11/27  15:42
 */
//对外接口
public class SwipeBack {
    public static List   <Bean_Activity> acts=new ArrayList<>();                     //保存所有需要侧滑的activity
    @SuppressLint("UseSparseArrays")
    private static Map<Integer,Integer> activity_stack=new HashMap<>();//这里维护activity栈       下标

    //新增加
    public static  Fragment mFragment;
    private static SwipeBackLayout mSwipeBackLayout;



    public static void init(Application app){
        app.registerActivityLifecycleCallbacks(new LifeCycleCallback_Activity());//注册activity生命周期回调
    }

//    public static List<Activity> getActivitys() {  return activitys; }

    public static void start(){

    }
    public static void start(Fragment fragment){

    }


//    public static void addActivity(Activity activity){
//        activitys.put(activity.getClass().getName(),activity) ;
//    }


    public static void remove(){}                 //移除当前activity最上层fragment
    public static void remove(int index){

    }
    public static void remove(Fragment fragment){

    } //移除

    public static Fragment findFragment(Class fragmentClass){
        Fragment fragment =null;
        return fragment;
    }
    public static Fragment findFragment(String tag){
        Fragment fragment =null;
        return fragment;
    }  //查找
    public static Fragment getTopFragment(){
        Fragment fragment =null;
        return fragment;
    }//获取当前activity最上面的fragment
    public static Fragment getPreFragment(){
        Fragment fragment =null;
        return fragment;
    }//获取当前fragment之前(栈下层)的一个fragment  扩展用

    public static void showHideFragment(Fragment showFragment, Fragment hideFragment){

    }  // 同级切换 show一个Fragment，hide一个Fragment；切换tab的情况


    public static SwipeBackLayout  getLayout(){
        Activity top =getTopActivity();
        Log.i("app","获取到的顶层activity名:"+top.getClass().getSimpleName());
        for(Bean_Activity act:SwipeBack.acts){
            if (act.activity.equals(top)){
                return act.swipeBackLayout;
            }
        }
        return null;
    }

    public static SwipeBackLayout  getFragmentLayout(){
        Activity top =getTopActivity();
        for(Bean_Activity act:SwipeBack.acts){
            if (act.activity.equals(top)){
                return act.swipeBackLayout;
            }
        }

        return null;
    }


    public static Activity getTopActivity(){

            ActivityManager am = (ActivityManager) acts.get(0).activity.getSystemService(ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            Log.d("app", "isTopActivity = " + cn.getClassName());

        for (Bean_Activity act:SwipeBack.acts){
            Log.i("app","----遍历时act名字"+cn.getClassName());
            Log.i("app","----遍历时act名字"+cn.getShortClassName());
            Log.i("app","----2遍历时act名字"+"."+act.activity.getClass().getSimpleName());
            if ( cn.getClassName().contains(act.activity.getClass().getSimpleName())){
                Log.i("app","获取到的顶层activity名:"+act.activity.getClass().getSimpleName());
                return act.activity;
            }
        }

        return null;
    }



    public static Fragment getTopChildFragment(){
        Fragment fragment =null;
        return fragment;
    }//获取fragment的顶层子fragment
    public static Fragment findChildFragment(Class fragmentClass){
        Fragment fragment =null;
        return fragment;
    }//查找fragment的子fragment
    public static Fragment findChildFragment(String tag){
        Fragment fragment =null;
        return fragment;
    }                 //




    public static View inject(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  onFragmentCreate(inflater,container,savedInstanceState);
    }

    private static View  onFragmentCreate(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mFragment.getContext() == null) return container;
        mSwipeBackLayout = new SwipeBackLayout(mFragment.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSwipeBackLayout.setLayoutParams(params);
        mSwipeBackLayout.setBackgroundColor(Color.TRANSPARENT);
        container.addView(mSwipeBackLayout);
        return  container;
    }

    private static void hideSoftInput(){

    } //隐藏输入法
    private static void showSoftInput(){

    }//显示输入法


    //activity背景设为透明 兼容
    public   static void convertActivityFromTranslucent(Activity activity) {
        try {
            @SuppressLint("PrivateApi") Method method = Activity.class.getDeclaredMethod("convertFromTranslucent");
            method.setAccessible(true);
            method.invoke(activity);
        } catch (Throwable t) { }
    }//转为全屏不透明
    public   static void convertActivityToTranslucent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertActivityToTranslucentAfterL(activity);
        } else {
            convertActivityToTranslucentBeforeL(activity);
        }
    }    //转换activity为透明
    private  static void convertActivityToTranslucentBeforeL(Activity activity) {
        try {
            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> translucentConversionListenerClazz = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz;
                }
            }
            @SuppressLint("PrivateApi") Method method = Activity.class.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz);
            method.setAccessible(true);
            method.invoke(activity, new Object[] {
                    null
            });
        } catch (Throwable t) { }
    }//在Android 5.0之前的平台上调用convertToTranslucent方法
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private  static void convertActivityToTranslucentAfterL(Activity activity) {
        try {
            @SuppressLint("PrivateApi") Method getActivityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
            getActivityOptions.setAccessible(true);
            Object options = getActivityOptions.invoke(activity);

            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> translucentConversionListenerClazz = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz;
                }
            }
            @SuppressLint("PrivateApi") Method convertToTranslucent =
                    Activity.class.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz, ActivityOptions.class);
            convertToTranslucent.setAccessible(true);
            convertToTranslucent.invoke(activity, null, options);
        } catch (Throwable t) {  }
    }//在Android 5.0之后的平台上调用convertToTranslucent方法
    //activity背景设为兼容



}
