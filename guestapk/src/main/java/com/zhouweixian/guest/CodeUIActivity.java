package com.zhouweixian.guest;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * Created by leochou on 2017/6/17 19:43
 */

public class CodeUIActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("a textview by new TextView()...");
        setContentView(textView);
    }
}
