package com.cosmos.dozy;

import android.app.Application;

import org.opencv.android.OpenCVLoader;
import org.opencv.osgi.OpenCVNativeLoader;

public class Dozy extends Application {
    static {
        OpenCVLoader.initDebug();
    }
}
