package com.flutter.android;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;

public class FlutterCordovaPlugin extends CordovaPlugin {
    private final static String CHANNEL_NAME = "app.channel.shared.cordova.data";
    private final static String FLUTTER_ENGINE_ID = "flutter_engine_id";
    private final static int FLUTTER_ACTIVITY_ID = 10;

    public static FlutterCordovaPlugin instance;
    CallbackContext openCallbackContext = null;

    public FlutterCordovaPlugin() {
        FlutterCordovaPlugin.instance = this;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        // 参考文档 https://flutter.dev/docs/development/add-to-app/android/add-flutter-screen
        if (action.equals("init")) {
            return prepareInitAction(args, callbackContext);
        }

        if (action.equals("open")) {
            return prepareOpenAction(args, callbackContext);
        }
        
        return false;
    }

    private boolean prepareInitAction(JSONArray args, final CallbackContext callbackContext) {
        this.cordova.getActivity().runOnUiThread(() -> {
            try {
                initFlutterEngine();
                // 初始化成功
                cordova.getThreadPool().execute(() -> {
                    callbackContext.success();
                });
            } catch (Exception ex) {
                // 初始化失败
                cordova.getThreadPool().execute(() -> {
                    callbackContext.error(ex.getMessage());
                });
            }
        });

        return true;
    }

    private boolean prepareOpenAction(JSONArray args, final CallbackContext callbackContext) {
        this.cordova.getActivity().runOnUiThread(() -> {
            try {
                open(args, callbackContext);
            } catch (Exception ex) {
                callbackContext.error(ex.getMessage());
            }
        });

        return true;
    }

    private void open(JSONArray args, final CallbackContext callbackContext) throws Exception {
        String routerName = null;
        if (args.length() > 0) {
            routerName = args.getString(0);
        }
        
        // Todo: invoke flutter method to navigate to routerName

        Intent intent = CordovaFlutterActivity
            .withCachedEngine(FLUTTER_ENGINE_ID)
            .build(FlutterCordovaPlugin.this.cordova.getActivity());

        FlutterCordovaPlugin.this
            .cordova
            .startActivityForResult(
                (CordovaPlugin) FlutterCordovaPlugin.this,
                intent,
                FLUTTER_ACTIVITY_ID
            );

        openCallbackContext = callbackContext;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // 这个方法桥接js，返回flutter的结果
        if (resultCode == Activity.RESULT_OK) {
            String result = intent.getStringExtra("result");
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    openCallbackContext.success(result);
                }
            });
        } else if (resultCode == Activity.RESULT_CANCELED) {

        }
    }

    private void initFlutterEngine() throws Exception  {
        FlutterEngine flutterEngine = new FlutterEngine(cordova.getContext());        
        flutterEngine.getDartExecutor().executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault());
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        FlutterEngineCache
            .getInstance()
            .put(FLUTTER_ENGINE_ID, flutterEngine);

        createMethodChannel(flutterEngine);
    }

    private void createMethodChannel(FlutterEngine flutterEngine) {
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

    private void finishFlutterActivity(HashMap<String, Object> arguments) {
        Intent intent = new Intent();

        JSONObject argJSONObject = new JSONObject(arguments);
        intent.putExtra("result", argJSONObject.toString());

        CordovaFlutterActivity.instance.setResult(Activity.RESULT_OK, intent);
        CordovaFlutterActivity.instance.finish();
    }
}
