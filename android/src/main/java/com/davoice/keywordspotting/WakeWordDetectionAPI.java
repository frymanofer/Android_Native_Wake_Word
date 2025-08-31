package com.davoice.keywordspotting;

import com.davoice.keywordsdetection.keywordslibrary.KeyWordsDetection;
import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import androidx.annotation.Nullable;
import ai.onnxruntime.*;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class KeyWordRNBridge extends ReactContextBaseJavaModule {

    private final String TAG = "KeyWordsDetection";
    private static final String REACT_CLASS = "KeyWordRNBridge";
    private static ReactApplicationContext reactContext;

    // Map to hold multiple instances
    private Map<String, KeyWordsDetection> instances = new HashMap<>();

    public KeyWordRNBridge(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void setKeywordDetectionLicense(String instanceId, String licenseKey, Promise promise) {
        KeyWordsDetection instance = instances.get(instanceId);
        Log.d(TAG, "setKeywordDetectionLicense()");

        Boolean isLicesed = false;
        if (instance != null) {
            isLicesed = instance.setLicenseKey(licenseKey);
        }
        Log.d(TAG, "setKeywordDetectionLicense(): " + (isLicesed ? "Licensed" : "Not Licensed"));

        promise.resolve(isLicesed);
    }

    // Create a new instance efficiently
    @ReactMethod
    public void createInstanceMulti(String instanceId, ReadableArray modelPathsArray, ReadableArray thresholdsArray, ReadableArray bufferCntsArray, ReadableArray msBetweenCallbackArray, Promise promise) {
        if (instances.containsKey(instanceId)) {
            promise.reject("InstanceExists", "Instance already exists with ID: " + instanceId);
            return;
        }

        try {
            int size = modelPathsArray.size();
            if (thresholdsArray.size() != size || bufferCntsArray.size() != size || msBetweenCallbackArray.size() != size) {
                promise.reject("InvalidArguments", "All input arrays must be the same length.");
                return;
            }

            // Convert ReadableArrays to Java arrays
            String[] modelPaths = new String[size];
            float[] thresholds = new float[size];
            int[] bufferCnts = new int[size];
            long[] msBetweenCallback = new long[size];

            for (int i = 0; i < size; i++) {
                modelPaths[i] = modelPathsArray.getString(i);
                thresholds[i] = (float) thresholdsArray.getDouble(i);
                bufferCnts[i] = bufferCntsArray.getInt(i);
                msBetweenCallback[i] = (long) msBetweenCallbackArray.getDouble(i); // RN uses Double for all numbers
            }

            // Create instance
            KeyWordsDetection keyWordsDetection = new KeyWordsDetection(reactContext, modelPaths, thresholds, bufferCnts, msBetweenCallback);
            keyWordsDetection.initialize((detected, modelName) -> onKeywordDetected(instanceId, detected, modelName));

            instances.put(instanceId, keyWordsDetection);
            promise.resolve("Multi-model instance created with ID: " + instanceId);

        } catch (Exception e) {
            promise.reject("CreateError", "Failed to create multi-model instance: " + e.getMessage());
        }
    }

    // Create a new instance
    @ReactMethod
    public void createInstance(String instanceId, String modelName, float threshold, int bufferCnt, Promise promise) {
        if (instances.containsKey(instanceId)) {
            promise.reject("InstanceExists", "Instance already exists with ID: " + instanceId);
            return;
        }

        try {
            KeyWordsDetection keyWordsDetection = new KeyWordsDetection(reactContext, modelName, threshold, bufferCnt);
            keyWordsDetection.initialize((detected, ignored) -> onKeywordDetected(instanceId, detected, modelName));

            instances.put(instanceId, keyWordsDetection);
            promise.resolve("Instance created with ID: " + instanceId);
        } catch (Exception e) {
            promise.reject("CreateError", "Failed to create instance: " + e.getMessage());
        }
    }

    @ReactMethod
    public void getRecordingWav(String instanceId, Promise promise) {
        KeyWordsDetection instance = instances.get(instanceId);
        String recordingWav = "";
        if (instance == null) {
            promise.reject("Instance not Exists", "Instance does not exists with ID: " + instanceId);
        }
        try {
            recordingWav = instance.getRecordingWav();
            promise.resolve(recordingWav);
        } catch (Exception e) {
            promise.reject("GetRecordingWavError", "Failed to get recording WAV: " + e.getMessage());
        }
    }

    // Create a new instance
    @ReactMethod
    public void replaceKeywordDetectionModel(String instanceId, String modelName, float threshold, int bufferCnt, Promise promise) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance == null) {
            promise.reject("Instance not Exists", "Instance does not exists with ID: " + instanceId);
            return;
        }

        try {
            instance.replaceKeywordDetectionModel(reactContext, modelName, threshold, bufferCnt);
            promise.resolve("Instance ID: " + instanceId + " change model " + modelName);
        } catch (Exception e) {
            promise.reject("CreateError", "Failed to create instance: " + e.getMessage());
        }
    }

    // Start detection for a specific instance
    @ReactMethod
    public void startKeywordDetection(String instanceId, float threshold, Promise promise) throws OrtException {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance != null) {
            instance.startListening(threshold);
            promise.resolve("Started detection for instance: " + instanceId);
        } else {
            promise.reject("InstanceNotFound", "No instance found with ID: " + instanceId);
        }
    }

    // Stop detection for a specific instance
    @ReactMethod
    public void stopForegroundService(String instanceId, Promise promise) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance != null) {
            instance.stopForegroundService();
            promise.resolve("stopForegroundService" + instanceId);
        } else {
            promise.reject("stopForegroundService", "No instance found with ID: " + instanceId);
        }
    }
    
    // Stop detection for a specific instance
    @ReactMethod
    public void startForegroundService(String instanceId, Promise promise) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance != null) {
            instance.startForegroundService();
            promise.resolve("startForegroundService" + instanceId);
        } else {
            promise.reject("startForegroundService", "No instance found with ID: " + instanceId);
        }
    }

    // Stop detection for a specific instance
    @ReactMethod
    public void stopKeywordDetection(String instanceId, Promise promise) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance != null) {
            instance.stopListening();
            promise.resolve("Stopped detection for instance: " + instanceId);
        } else {
            promise.reject("InstanceNotFound", "No instance found with ID: " + instanceId);
        }
    }

    // Destroy an instance
    @ReactMethod
    public void destroyInstance(String instanceId, Promise promise) {
        KeyWordsDetection instance = instances.remove(instanceId);
        if (instance != null) {
            instance.stopListening();
            // Additional cleanup if necessary
            promise.resolve("Destroyed instance: " + instanceId);
        } else {
            promise.reject("InstanceNotFound", "No instance found with ID: " + instanceId);
        }
    }

    // Handle keyword detection event
    private void onKeywordDetected(String instanceId, boolean detected, String modelName) {
        if (detected) {
            WritableMap params = Arguments.createMap();
            params.putString("instanceId", instanceId);
            params.putString("phrase", modelName);
            params.putString("modelName", modelName);
            sendEvent("onKeywordDetectionEvent", params);
        }
    }

    // Send event to JavaScript
    private void sendEvent(String eventName, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    @ReactMethod
    public void addListener(String eventName) {
        // Set up any upstream listeners or background tasks as necessary
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Remove upstream listeners, stop unnecessary background tasks
    }
    // Implement other methods as needed, ensuring to use instanceId
}
