package com.davoice.keywordspotting;

import com.davoice.keywordsdetection.keywordslibrary.KeyWordsDetection;
import androidx.annotation.Nullable;
import ai.onnxruntime.*;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

public class KeyWordDetectionAPI {

    private final String TAG = "KeyWordsDetection";
    private Context context;

    // Map to hold multiple instances
    private Map<String, KeyWordsDetection> instances = new HashMap<>();

    public KeyWordDetectionAPI(Context context) {
        this.context = context;
    }

    // Set license for a specific instance
    public boolean setKeywordDetectionLicense(String instanceId, String licenseKey) {
        KeyWordsDetection instance = instances.get(instanceId);
        Log.d(TAG, "setKeywordDetectionLicense()");

        if (instance != null) {
            boolean isLicensed = instance.setLicenseKey(licenseKey);
            Log.d(TAG, "setKeywordDetectionLicense(): " + (isLicensed ? "Licensed" : "Not Licensed"));
            return isLicensed;
        } else {
            Log.e(TAG, "Instance not found for ID: " + instanceId);
            return false;
        }
    }

    // Create a new instance
    public boolean createInstance(String instanceId, String modelName, float threshold, int bufferCnt) {
        if (instances.containsKey(instanceId)) {
            Log.e(TAG, "Instance already exists with ID: " + instanceId);
            return false;
        }

        try {
            KeyWordsDetection keyWordsDetection = new KeyWordsDetection(context, modelName, threshold, bufferCnt);
            keyWordsDetection.initialize(detected -> onKeywordDetected(instanceId, detected));
            instances.put(instanceId, keyWordsDetection);
            Log.d(TAG, "Instance created with ID: " + instanceId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to create instance: " + e.getMessage());
            return false;
        }
    }

    // Get the recording WAV file for a specific instance
    public String getRecordingWav(String instanceId) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance == null) {
            Log.e(TAG, "Instance does not exist with ID: " + instanceId);
            return null;
        }

        try {
            return instance.getRecordingWav();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get recording WAV: " + e.getMessage());
            return null;
        }
    }

    // Replace the keyword detection model
    public boolean replaceKeywordDetectionModel(String instanceId, String modelName, float threshold, int bufferCnt) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance == null) {
            Log.e(TAG, "Instance does not exist with ID: " + instanceId);
            return false;
        }

        try {
            instance.replaceKeywordDetectionModel(context, modelName, threshold, bufferCnt);
            Log.d(TAG, "Model replaced for instance ID: " + instanceId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to replace model: " + e.getMessage());
            return false;
        }
    }

    // Start detection for a specific instance
    public boolean startKeywordDetection(String instanceId, float threshold) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance != null) {
            try {
                instance.startListening(threshold);
                Log.d(TAG, "Started detection for instance: " + instanceId);
                return true;
            } catch (OrtException e) {
                Log.e(TAG, "Failed to start detection: " + e.getMessage());
                return false;
            }
        } else {
            Log.e(TAG, "No instance found with ID: " + instanceId);
            return false;
        }
    }

    // Stop detection for a specific instance
    public boolean stopKeywordDetection(String instanceId) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance != null) {
            instance.stopListening();
            Log.d(TAG, "Stopped detection for instance: " + instanceId);
            return true;
        } else {
            Log.e(TAG, "No instance found with ID: " + instanceId);
            return false;
        }
    }

    // Start foreground service for a specific instance
    public boolean startForegroundService(String instanceId) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance != null) {
            instance.startForegroundService();
            Log.d(TAG, "Foreground service started for instance: " + instanceId);
            return true;
        } else {
            Log.e(TAG, "No instance found with ID: " + instanceId);
            return false;
        }
    }

    // Stop foreground service for a specific instance
    public boolean stopForegroundService(String instanceId) {
        KeyWordsDetection instance = instances.get(instanceId);
        if (instance != null) {
            instance.stopForegroundService();
            Log.d(TAG, "Foreground service stopped for instance: " + instanceId);
            return true;
        } else {
            Log.e(TAG, "No instance found with ID: " + instanceId);
            return false;
        }
    }

    // Destroy an instance
    public boolean destroyInstance(String instanceId) {
        KeyWordsDetection instance = instances.remove(instanceId);
        if (instance != null) {
            instance.stopListening();
            Log.d(TAG, "Destroyed instance: " + instanceId);
            return true;
        } else {
            Log.e(TAG, "No instance found with ID: " + instanceId);
            return false;
        }
    }

    // Handle keyword detection event
    private void onKeywordDetected(String instanceId, Boolean detected) {
        if (detected) {
            Log.d(TAG, "Keyword detected for instance ID: " + instanceId);
            // You can implement a listener here to notify other components
        }
    }
}
