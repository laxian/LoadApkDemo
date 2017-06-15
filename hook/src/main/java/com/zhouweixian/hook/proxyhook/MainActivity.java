package com.zhouweixian.hook.proxyhook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zhouweixian.hook.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button bt_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        bt_main = (Button) findViewById(R.id.bt_main);

        bt_main.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_main:
                Intent intent = new Intent(this, SecondActivity.class);
                intent.putExtra("a", "b");
                startActivity(intent);
                finish();
                break;
        }
    }
}
