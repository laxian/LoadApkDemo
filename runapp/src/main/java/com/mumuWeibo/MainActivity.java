package com.mumuWeibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    Button btOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btOpen = (Button) findViewById(R.id.bt_open);
        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName(MainActivity.this.getPackageName(), "com.mumuWeibo2" +
                        ".SplashScreen");
                MainActivity.this.startActivityForResult(intent, 11);

            }
        });

        findViewById(R.id.bt_open_self).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 11) {
//            try {
//                ObjectInputStream in = new ObjectInputStream(new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + ".obj"));
//                PackageInfo packageInfo = (PackageInfo) in.readObject();
//                ReflectionUtils.setField(AppApplication.instance, "mPackageInfo", packageInfo);
//            } catch (IOException | ClassNotFoundException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
