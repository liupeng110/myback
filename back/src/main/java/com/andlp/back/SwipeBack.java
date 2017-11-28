package com.andlp.back;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.os.Build;
import android.support.v4.app.Fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * 717219917@qq.com      2017/11/27  15:42
 */
//对外接口
public class SwipeBack {
//    private static List   <Activity> activitys=new ArrayList<>();                     //保存所有需要侧滑的activity
    @SuppressLint("UseSparseArrays")
    private static Map<Integer,Integer> activity_stack=new HashMap<>();//这里维护activity栈       下标

    private static Map<String ,Activity >  activitys  =new HashMap<>();

    public static void init(Application app){
        app.registerActivityLifecycleCallbacks(new LifeCycleCallback_Activity());//注册activity生命周期回调
    }

//    public static List<Activity> getActivitys() {  return activitys; }

    public static void start(){

    }
    public static void start(Fragment fragment){

    }


    public static void addActivity(Activity activity){
        activitys.put(activity.getClass().getName(),activity) ;
    }


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
