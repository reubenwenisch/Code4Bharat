package com.cosmos.dozy.activities;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cosmos.dozy.services.BackgroundImageProcessor;

public class MainActivity extends AppCompatActivity  {

    private static final int REQ_CODE_CAMERA_PERMISSION = 1253;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},REQ_CODE_CAMERA_PERMISSION);
        }

        else{
            startService(new Intent(MainActivity.this, BackgroundImageProcessor.class));
        }

    }

}
