package com.andlp.myback.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andlp.back.SwipeBack;

/**
 * 717219917@qq.com      2017/12/1  16:05
 */

public class BaseFragment extends Fragment {
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return SwipeBack.inject(inflater, container, savedInstanceState);
    }


}
