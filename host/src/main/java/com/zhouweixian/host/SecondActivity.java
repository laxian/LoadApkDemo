package com.zhouweixian.host;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.zhouweixian.host.hook.HookHelper;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt_main;
    private Button bt_stub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initView();

        HookHelper.hookActivityManagerNative();
        HookHelper.hookActivityThreadHandler();
    }

    private void initView() {
        bt_main = (Button) findViewById(R.id.bt_main);
        bt_stub = (Button) findViewById(R.id.bt_stub);

        bt_main.setOnClickListener(this);
        bt_stub.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_main:
                startActivity(new Intent(this, RealActivity.class));
                break;
            case R.id.bt_stub:

                break;
        }
    }
}
