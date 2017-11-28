package com.andlp.back.bean;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.andlp.back.SwipeBackLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 717219917@qq.com      2017/11/27  15:47
 */
//封装单个activity中需要保存的信息
public class Bean_Activity {

    public String   tag="";                                 //tag(类名) 便于查找比对
    public Activity activity=null;                       //自身activity
    public SwipeBackLayout swipeBackLayout =null;
    public Fragment fragment=null;               //自身fragment
    public List<Fragment> childFragemtns=new ArrayList<>();  //所有的子fragment
    public   boolean isOnResume =false;          //是否正在显示


    public Bean_Activity(Activity act, SwipeBackLayout layout){
        activity=act;
        swipeBackLayout=layout;
        tag=act.getClass().getSimpleName();
    }


}
