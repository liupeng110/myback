package com.andlp.back;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 717219917@qq.com      2017/11/27  14:45
 */

public class LifeCycleCallback_Fragment extends FragmentManager.FragmentLifecycleCallbacks{

    public void onFragmentPreAttached(FragmentManager fm, Fragment f, Context context) {
        Log.i("app","进入fragment+++++++++++++++++++++");
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentPreAttached();");
    }
    public void onFragmentAttached(FragmentManager fm, Fragment f, Context context) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentAttached();");

//        mSwipeBackLayout.attachToActivity(activity);//每次创建activity 就创建一个根布局

    }

    public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentCreated();");


    }
    public void onFragmentActivityCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentActivityCreated();");
    }
    public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentViewCreated();");
//        SwipeBackLayout   mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(f.getActivity()).inflate( R.layout.swipeback_layout, null);
//        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_ALL );
//        ((ViewGroup) v.getParent()).removeView(v);
//        ((ViewGroup)f.getActivity().findViewById(android.R.id.content)).removeView(v);

//        mSwipeBackLayout.addView(f.getView());

//        ((ViewGroup)f.getActivity().findViewById(android.R.id.content)).addView(v);
//        ((ViewGroup) v.getParent()).addView(v);

//        mSwipeBackLayout


    }
    public void onFragmentStarted(FragmentManager fm, Fragment f) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentStarted();");
    }
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentResumed();");
    }
    public void onFragmentPaused(FragmentManager fm, Fragment f) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentPaused();");
    }
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentStopped();");
    }
    public void onFragmentSaveInstanceState(FragmentManager fm, Fragment f, Bundle outState) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentSaveInstanceState ();");
    }
    public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentViewDestroyed();");
    }
    public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentDestroyed();");
    }
    public void onFragmentDetached(FragmentManager fm, Fragment f) {
        Log.i("app","进入---------- "+f.getClass().getSimpleName()+".onFragmentDetached();");
        Log.i("app","进入---------- Fragment----------------------------------");
    }



}
