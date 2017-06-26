package com.zhouweixian.guest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

/**
 * 测试从Host app 跳转到guest app,加载资源正确
 * 测试从guest app 跳转回到host app
 */
public class HotFixActivity extends Activity implements View.OnClickListener {

    private static final String TAG = HotFixActivity.class.getSimpleName();
    private Button bt_test;
    private TextView tv_info;
    private Button bt_start_host;

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
        bt_start_host = (Button) findViewById(R.id.bt_start_host);
        bt_start_host.setOnClickListener(this);
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
                tv_info.setText(test_dir.exists() ? test_dir.getPath() : "create failed");
                Log.d(TAG, test_dir.exists() ? test_dir.getPath() : "create failed");
                break;
            case R.id.bt_start_host:
                Intent intent = new Intent();
                intent.setClassName("com.zhouweixian.host", "com.zhouweixian.host.MainActivity");
                startActivity(intent);
                break;
        }
    }
}
