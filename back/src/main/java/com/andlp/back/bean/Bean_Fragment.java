package com.andlp.back.bean;

import android.support.v4.app.Fragment;

import java.util.List;

/**
 * 717219917@qq.com      2017/11/27  15:47
 */
//封装单个fragment中需要保存的信息
public class Bean_Fragment {
      String tag="";                                              //自身tag
      Fragment fragment=null;                          //自身fragment
      List<Fragment> child2Fragemtns=null;  //所有的子fragment
      List<Integer>               child2stack=null;  //需要维护的child2stack栈
    boolean isOnResume =false;          //是否正在显示



}
