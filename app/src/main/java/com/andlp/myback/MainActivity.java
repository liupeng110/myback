package com.andlp.myback;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.andlp.back.SwipeBack;
import com.andlp.back.SwipeBackLayout;
import com.andlp.myback.fragment.MyFragment;

public class MainActivity extends AppCompatActivity {
    MyFragment fragment= new MyFragment();
    Button btn1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 =  findViewById(R.id.btn1);
        new Thread(getRunnable()).start();
            SwipeBack.getLayout().setEnabled(false);
        SwipeBack.getLayout().setEnableGesture(false);
//         SwipeBack.getLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_ALL);

    }

    private Runnable getRunnable(){
        return new Runnable() {
            @Override public void run() {
                btn1.setText("我是子线程new");
            }
        };
    }


    public void btn1(View view){
        Log.i("app","点击btn1");
        Fragment fragment_tmp = getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
        if (fragment_tmp==null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// fragment.onAttachFragment(fragment_tmp);
            transaction.add(R.id.main, fragment,fragment.getClass().getSimpleName());
            transaction.commit();
        }else {getSupportFragmentManager().beginTransaction().show(fragment_tmp).commit(); }


    }
    public void btn2(View view){
        Log.i("app","点击btn2");
        getSupportFragmentManager().beginTransaction().hide(fragment).commit();
    }

    public void txt(View view){
        Log.i("app","点击txt");
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(
                new Runnable() {
                    @Override public void run() {
                        btn1.setText("我是子线程");
                    }
                }
        ).start();
    }
}
