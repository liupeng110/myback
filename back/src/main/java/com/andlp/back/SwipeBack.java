package com.andlp.back;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * 717219917@qq.com      2017/11/27  15:42
 */
//对外接口
public class SwipeBack {
    private static List   <Activity> activitys;                     //保存所有需要侧滑的activity
    private static Map<Integer,Integer> activity_stack;//这里维护activity栈       下标

    public static void init(Application app){
        app.registerActivityLifecycleCallbacks(new LifeCycleCallback_Activity());//注册activity生命周期回调
    }

    public static List<Activity> getActivitys() {
        return activitys;
    }

    public static void start(){

    }
    public static void start(Fragment fragment){

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






}
