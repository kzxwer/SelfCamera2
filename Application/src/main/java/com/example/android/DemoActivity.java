package com.example.android;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


import com.example.android.camera2basic.base.BaseActivity;
import com.example.android.camera2basic.MediaType;
import com.example.android.camera2basic.ScaleType;
import com.example.android.camera2basic.picker.MediaPickerOpts;
import com.example.android.camera2basic.picker.Result;

import java.io.File;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
//gitgit

/**
 * Created by Pankaj Soni <pankajsoni@softwarejoint.com> on 03/03/18.
 * Copyright (c) 2018 Software Joint. All rights reserved.
 */
public class DemoActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "DemoActivity";

    @Override
    protected void onCreate(@org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);

        setTransition(Gravity.END, GravityCompat.END, GravityCompat.END, GravityCompat.END);
        //setContentView(R.layout.demo_activity);
        startImagePicker();

        //findViewById(R.id.image).setOnClickListener(this);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        new EncoderTest().testAACEncoders();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Result result = MediaPickerOpts.onActivityResult(requestCode, resultCode, data);
        if (result != null) {
            StringBuilder builder = new StringBuilder();
            for (String file: result.files) {
                builder.append(new File(file).getName()).append("\r\n");
                Log.d(TAG, "file: picked: " + file);
            }

        }
    }

    private void startImagePicker() {
        new MediaPickerOpts.Builder()
                .setMediaType(MediaType.IMAGE)
                .withCameraType(ScaleType.SCALE_SQUARE)
                .withGallery(Boolean.valueOf("true"))
                .withFlash(Boolean.valueOf("true"))
                .withFilters(Boolean.valueOf("true"))
                .withCropEnabled(Boolean.valueOf("true"))
                .canChangeScaleType(Boolean.valueOf("true"))
                .withImgSize(Integer.valueOf("96"))
                .withMaxSelection(Integer.parseInt("2"))
                .build()
                .startActivity(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.image:
                startImagePicker();
                break;
        }
    }
}