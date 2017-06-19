package com.zhouweixian.guest;

import android.app.Activity;
import android.os.Bundle;

/**
 * 从host app里加载启动这个activity，由于资源id重复，尽管启动activity成功，但是加载的布局，却不是
 * R.layout.activity_hot_fix对应的布局，而是host app里，资源id等于R.layout.activity_hot_fix的一个布局
 */
public class HotFixActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_fix);
    }
}
