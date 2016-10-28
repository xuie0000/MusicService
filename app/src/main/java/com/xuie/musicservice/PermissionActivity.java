package com.xuie.musicservice;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.tbruyelle.rxpermissions.RxPermissions;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "PermissionActivity";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        Log.d(TAG, "onCreate.");

        RxPermissions.getInstance(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        startActivity(new Intent(this, MainActivity.class));
                    }
                    finish();
                });
    }
}
