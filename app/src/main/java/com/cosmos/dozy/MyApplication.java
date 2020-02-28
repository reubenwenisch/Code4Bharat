package com.cosmos.dozy;

import android.app.Application;

import org.opencv.android.OpenCVLoader;

public class MyApplication extends Application {
    static {
        OpenCVLoader.initDebug();
    }

}
