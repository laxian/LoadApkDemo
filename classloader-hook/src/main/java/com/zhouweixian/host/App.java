package com.zhouweixian.host;

import android.app.Application;
import android.content.Context;

/**
 * Created by leochou on 2017/6/17 20:40
 */

public class App extends Application {

    public static App instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        instance = this;
    }
}
