package com.flutter.android;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.CordovaInterface;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;

import java.util.HashMap;

import org.json.JSONObject;

public class FlutterEngineManager {
    private final static String CHANNEL_NAME = "app.channel.shared.cordova.data";

    public static void initFlutterEngine(CordovaInterface cordova, String engineId) throws Exception  {
        FlutterEngine flutterEngine = new FlutterEngine(cordova.getContext());        
        flutterEngine.getDartExecutor().executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault());
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        FlutterEngineCache
            .getInstance()
            .put(engineId, flutterEngine);

        createMethodChannel(flutterEngine);
    }

    private static void createMethodChannel(FlutterEngine flutterEngine) {
        MethodChannel methodChannel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL_NAME);
        methodChannel.setMethodCallHandler((call, result) -> {
            HashMap<String, Object> arguments = (HashMap<String, Object>) call.arguments;

            if (call.method.equals("finish")) {
                finishFlutterActivity(arguments);
                result.success(true);
                return;
            }

            result.notImplemented();
        });
    }

    private static void finishFlutterActivity(HashMap<String, Object> arguments) {
        Intent intent = new Intent();

        JSONObject argJSONObject = new JSONObject(arguments);
        intent.putExtra("result", argJSONObject.toString());

        CordovaFlutterActivity.instance.setResult(Activity.RESULT_OK, intent);
        CordovaFlutterActivity.instance.finish();
    }
}
