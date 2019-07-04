/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.example.jiexingxing.mysaoyisao.zxing.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jiexingxing.mysaoyisao.R;
import com.example.jiexingxing.mysaoyisao.zxing.camera.CameraManager;
import com.example.jiexingxing.mysaoyisao.zxing.decode.DecodeThread;
import com.example.jiexingxing.mysaoyisao.zxing.utils.BeepManager;
import com.example.jiexingxing.mysaoyisao.zxing.utils.CaptureActivityHandler;
import com.example.jiexingxing.mysaoyisao.zxing.utils.InactivityTimer;
import com.google.zxing.Result;



import java.io.IOException;
import java.lang.reflect.Field;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback  {

   int   Tag=40;/*
   我设置的光线传感器阈值
   小于40会可以出现打开手电筒的标志   点击可以打开手电筒（可以在稍微黑暗的环境的时候就会出现图标）

   如果想自动开启关闭     全局搜索  111111111111111111   的逻辑放到 222222222222222222222222中去就好了



   */





    private static final String TAG = CaptureActivity.class.getSimpleName();
    boolean isopen=true;
    private Camera camera;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager manager;// 声明CameraManager对象

    private SurfaceView scanPreview = null;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;

//    private ImageView ivTitleLeft;
//    private TextView tvTitleCenter;
//    private TextView tvTitleRight;

    private Rect mCropRect = null;
    private boolean isHasSurface = false;
//    private MyfriendPresenter presenter;
    private TextView resulttext;
    private ImageView openlight;
    private TextView lighttext;
    private float lightLux;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_capture);
        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        resulttext = (TextView) findViewById(R.id.resulttext);
        openlight = (ImageView) findViewById(R.id.openlight);
        lighttext = (TextView) findViewById(R.id.lighttext);
        TextView  back = (TextView) findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation
                .RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);


        openlight.setVisibility(View.GONE);
        lighttext.setVisibility(View.GONE);

/*
*
* 光线传感器
*
*
*
* */

        SensorManager senserManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = senserManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        senserManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);


//222222222222222222222222     手动开启手电筒和关闭手电筒
        scanCropView.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {



                camera = cameraManager.getCamera();

                try {
                    Camera.Parameters parameters = camera.getParameters();
                    if (isopen) {
                        if (parameters.getFlashMode().equals("torch")) {
                            return;
                        } else {
                            parameters.setFlashMode("torch");
                        }
                    } else {
                        if (parameters.getFlashMode().equals("off")) {
                            return;
                        } else {
                            parameters.setFlashMode("off");
                        }
                    }
                    camera.setParameters(parameters);
                } catch (Exception e) {
                    finishFlashUtils();
                }
                isopen = !isopen;





            }
        });
    }



    public void finishFlashUtils() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
        camera = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
if (cameraManager==null){cameraManager = new CameraManager(getApplication());}


        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(scanPreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            scanPreview.getHolder().addCallback(this);
        }

        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param bundle    The extras
     */
    public void handleDecode(final Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        resulttext.setText(""+rawResult.getText());

//        }


    }



    private void initCamera(SurfaceHolder surfaceHolder) {


        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }

            initCrop();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);



            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {


//        ToastUtils.showLong(this, "请在手机的“设置-应用-好司机日记-权限”选项中，允许好司机日记访问您的相机");


    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        /* 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /* 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /* 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /* 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /* 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /* 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /* 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }





//      111111111111111111
    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //当传感器精度发生变化时
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            //当传感器监测到的数值发生变化时
            float value = event.values[0];
            if (value<Tag){
                openlight.setVisibility(View.VISIBLE);
                lighttext.setVisibility(View.VISIBLE);
            }else{
                openlight.setVisibility(View.GONE);
                lighttext.setVisibility(View.GONE);


            }



        }

    };


}