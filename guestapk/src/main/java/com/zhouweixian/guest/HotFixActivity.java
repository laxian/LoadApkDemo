package com.zhouweixian.guest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

/**
 * 从host app里加载启动这个activity，由于资源id重复，尽管启动activity成功，但是加载的布局，却不是
 * R.layout.activity_hot_fix对应的布局，而是host app里，资源id等于R.layout.activity_hot_fix的一个布局
 */
public class HotFixActivity extends Activity implements View.OnClickListener {

    private Button bt_test;
    private TextView tv_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_fix);
        initView();
    }

    private void initView() {
        bt_test = (Button) findViewById(R.id.bt_test);

        bt_test.setOnClickListener(this);
        tv_info = (TextView) findViewById(R.id.tv_info);
        tv_info.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_test:
                File test_dir = getFileStreamPath("test_dir");
                try {
                    test_dir.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tv_info.setText(test_dir.exists()?"test success":"test failed");
                break;
        }
    }
}
