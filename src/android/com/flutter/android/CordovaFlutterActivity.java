package com.flutter.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

import java.util.HashMap;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;



public class CordovaFlutterActivity extends io.flutter.embedding.android.FlutterActivity {
    public static CordovaFlutterActivity instance;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CordovaFlutterActivity.instance = this;
    }

    public static CachedEngineIntentBuilder withCachedEngine(String cachedEngineId) {
        return new CachedEngineIntentBuilder(CordovaFlutterActivity.class, cachedEngineId);
    }
}
