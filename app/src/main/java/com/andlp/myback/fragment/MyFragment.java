package com.andlp.myback.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andlp.back.SwipeBack;
import com.andlp.back.SwipeBackLayout;
import com.andlp.myback.R;

/**
 * 717219917@qq.com      2017/11/29  14:41
 */

public class MyFragment extends Fragment {
    TextView fragment_txt;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        SwipeBackLayout   mSwipeBackLayout = new SwipeBackLayout(getContext());
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        mSwipeBackLayout.setLayoutParams(params);
        SwipeBackLayout   mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(getContext()).inflate( com.andlp.back.R.layout.swipeback_layout, null);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_ALL );

//        mSwipeBackLayout.setBackgroundColor(Color.TRANSPARENT);
//        mSwipeBackLayout.setBackgroundColor(Color.CYAN);
//        container.addView(mSwipeBackLayout);


             View view = inflater.inflate(R.layout.fragment_my, mSwipeBackLayout, false);
              fragment_txt =view.findViewById(R.id.fragment_txt);
              fragment_txt.setOnClickListener(new View.OnClickListener() {
                  @Override public void onClick(View view) {
                      Log.i("app","点击了fragment中的text");
                  }
              });
//        mSwipeBackLayout.addView(view);
        mSwipeBackLayout.setmFragment(this);
        mSwipeBackLayout.setmContentView(view);

//        mSwipeBackLayout.removeAllViews();
//        mSwipeBackLayout.addView(view);
//        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_ALL);
//        return  view;
        return mSwipeBackLayout;
    }


}
