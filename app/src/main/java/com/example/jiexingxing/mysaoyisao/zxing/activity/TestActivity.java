package com.example.jiexingxing.mysaoyisao.zxing.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jiexingxing.mysaoyisao.R;
import com.example.jiexingxing.mysaoyisao.zxing.encoding.EncodingUtils;

/**
 * Created by jiexingxing on 2017/12/22.
 */

public class TestActivity extends Activity {


    private EditText test_et;
    private TextView nobitmap;
    private TextView addbitmap;
    private ImageView imageview;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        test_et = (EditText) findViewById(R.id.test_et);
        nobitmap = (TextView) findViewById(R.id.nobitmap);
        addbitmap = (TextView) findViewById(R.id.addbitmap);
        imageview = (ImageView) findViewById(R.id.imageview);



        nobitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(test_et.getText().toString().trim())){

                 Bitmap  bit=   EncodingUtils.createQRCode(test_et.getText().toString().trim(),200,200,null);


                 imageview.setImageBitmap(bit);
                }

            }
        });

        addbitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

                if (!TextUtils.isEmpty(test_et.getText().toString().trim())){
                    Bitmap  bit=   EncodingUtils.createQRCode(test_et.getText().toString().trim(),200,200,bitmap);
                    imageview.setImageBitmap(bit);
                }

            }
        });

    }
}
