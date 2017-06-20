package com.zhouweixian.hook.proxyhook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zhouweixian.hook.R;

public class SecondActivity extends BaseActivity implements View.OnClickListener {

    private Button bt_second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initView();
    }

    private void initView() {
        bt_second = (Button) findViewById(R.id.bt_second);

        bt_second.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_second:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
    }
}
