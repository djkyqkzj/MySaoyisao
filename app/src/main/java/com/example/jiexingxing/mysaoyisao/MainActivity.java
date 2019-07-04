package com.example.jiexingxing.mysaoyisao;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jiexingxing.mysaoyisao.zxing.activity.CaptureActivity;
import com.example.jiexingxing.mysaoyisao.zxing.activity.TestActivity;

public class MainActivity extends Activity {


    private TextView test;
    private TextView saoyisao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test = (TextView) findViewById(R.id.test);
        saoyisao = (TextView) findViewById(R.id.saoyisao);
        saoyisao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA)) {

//
                        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                        startActivity(intent);
                    } else {
                        //提示用户开户权限
                        String[] perms = {"android.permission.CAMERA"};
                        ActivityCompat.requestPermissions(MainActivity.this, perms, 301);
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    startActivity(intent);
                }

            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 301:
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted) {
                    //授权成功之后，调用系统相机进行拍照操作等
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    startActivity(intent);
                } else {
                    //用户授权拒绝之后，友情提示一下就可以了
//                    ToastUtils.showLong(this, "请在手机的“设置-应用-好司机日记-权限”选项中，允许好司机日记访问您的相机");

                    Toast.makeText(MainActivity.this,"请在手机的“设置-应用”选项中，允许本应用访问您的相机",Toast.LENGTH_SHORT).show();
                }

                break;




    }
    }
}
