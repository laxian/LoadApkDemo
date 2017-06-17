package com.zhouweixian.host.hook;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Field;

/**
 * Created by leochou on 2017/6/16 15:01
 */

class CustomActivityCallback implements Handler.Callback {

    private static final String RAW_INTENT_EXTRA = "raw_intent";
    private final Handler base;

    public CustomActivityCallback(Handler mH) {
        this.base = mH;
    }

    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == 100) {
            // LAUNCH_ACTIVITY         = 100;
             Object acr = msg.obj;
            try {
                Field intentField = acr.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                Intent intent = (Intent) intentField.get(acr);
                if (intent.getExtras() != null) {
                    Intent realIntent = (Intent) intent.getExtras().get(RAW_INTENT_EXTRA);
                    intent.setComponent(realIntent.getComponent());
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        base.handleMessage(msg);
        return true;
    }
}
