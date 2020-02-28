package com.cosmos.dozy.services;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.cosmos.dozy.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BackgroundCamService extends HiddenCameraService {

    private File mCascadeFile;
    private File cascadeFileER;
    private File cascadeFileEL;
    private File cascadeFileEyeOpen;

    private CascadeClassifier mJavaDetector;
    private CascadeClassifier mJavaDetectorEyeRight;
    private CascadeClassifier mJavaDetectorEyeLeft;
    private CascadeClassifier mJavaDetectorEyeOpen;
    private static final String TAG = "OpenCV_sleep_detection";

    private BaseLoaderCallback loaderCallBack = new BaseLoaderCallback() {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                        // load cascade file from application resources
                        //Face detection classifier
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // ------------------ load right eye classifier -----------------------
                        InputStream iser = getResources().openRawResource(R.raw.haarcascade_righteye_2splits);
                        File cascadeDirER = getDir("cascadeER",Context.MODE_PRIVATE);
                        cascadeFileER = new File(cascadeDirER,"haarcascade_eye_right.xml");
                        FileOutputStream oser = new FileOutputStream(cascadeFileER);

                        byte[] bufferER = new byte[4096];
                        int bytesReadER;
                        while ((bytesReadER = iser.read(bufferER)) != -1) {
                            oser.write(bufferER, 0, bytesReadER);
                        }
                        iser.close();
                        oser.close();

                        // ------------------ load left eye classifier -----------------------
                        InputStream isel = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
                        File cascadeDirEL = getDir("cascadeEL",Context.MODE_PRIVATE);
                        cascadeFileEL = new File(cascadeDirEL,"haarcascade_eye_left.xml");
                        FileOutputStream osel = new FileOutputStream(cascadeFileEL);

                        byte[] bufferEL = new byte[4096];
                        int bytesReadEL;
                        while ((bytesReadEL = isel.read(bufferEL)) != -1) {
                            osel.write(bufferEL, 0, bytesReadEL);
                        }
                        isel.close();
                        osel.close();

                        // ------------------ load open eye classifier -----------------------
                        InputStream opisel = getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
                        File cascadeDirEyeOpen = getDir("cascadeEyeOpen",Context.MODE_PRIVATE);
                        cascadeFileEyeOpen = new File(cascadeDirEyeOpen,"haarcascade_eye_tree_eyeglasses.xml");
                        FileOutputStream oposel = new FileOutputStream(cascadeFileEyeOpen);

                        byte[] bufferEyeOpen = new byte[4096];
                        int bytesReadEyeOpen;
                        while ((bytesReadEyeOpen = opisel.read(bufferEyeOpen)) != -1) {
                            oposel.write(bufferEyeOpen, 0, bytesReadEyeOpen);
                        }
                        opisel.close();
                        oposel.close();

                        //Face Classifier
                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier of face");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from "+ mCascadeFile.getAbsolutePath());
                        //cascadeDir.delete();

                        //EyeRightClassifier
                        mJavaDetectorEyeRight = new CascadeClassifier(cascadeFileER.getAbsolutePath());
                        if (mJavaDetectorEyeRight.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier of eye right");
                            mJavaDetectorEyeRight = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from "+ cascadeFileER.getAbsolutePath());
                        //cascadeDirER.delete();

                        //EyeLeftClassifier
                        mJavaDetectorEyeLeft = new CascadeClassifier(cascadeFileEL.getAbsolutePath());
                        if (mJavaDetectorEyeLeft.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier of eye left");
                            mJavaDetectorEyeLeft = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from "+ cascadeFileEL.getAbsolutePath());
                        //cascadeDirEL.delete();

                        //EyeOpenClassifier
                        mJavaDetectorEyeOpen = new CascadeClassifier(cascadeFileEyeOpen.getAbsolutePath());
                        if (mJavaDetectorEyeOpen.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier of eye open");
                            mJavaDetectorEyeOpen = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from "+ cascadeFileEyeOpen.getAbsolutePath());
                        //cascadeDirEyeOpen.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    mOpenCvCameraView.setCameraIndex(cameraid);
                    mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                CameraConfig cameraConfig = new CameraConfig()
                        .getBuilder(this)
                        .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        .build();

                startCamera(cameraConfig);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BackgroundCamService.this,
                                "Capturing image.", Toast.LENGTH_SHORT).show();

                        takePicture();
                    }
                }, 2000L);
            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        Toast.makeText(this,
                "Captured image size is : " + imageFile.length(),
                Toast.LENGTH_SHORT)
                .show();

        // Do something with the image...

        stopSelf();
    }

    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                break;
        }

        stopSelf();
    }

    private void loadOpenCVFunctionsAndFiles(){

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3,this,loaderCallBack)
    }

    public void startSleepDetection(){

    }
    //TODO: Vibrate device and sound ringtone for 10 seconds
    public void alertDriver(){
        try{
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    r.stop();
                }
            }, 10000);

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }

        }catch (Exception e){e.printStackTrace();}
    }
}
