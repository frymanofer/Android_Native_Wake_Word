package com.davoice.keywordspotting;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davoice.keywordsdetection.keywordslibrary.KeyWordsDetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.onnxruntime.OrtException;

/**
 * KeywordDetectionAPI
 *
 * A lightweight, native (non-React) manager for multiple KeyWordsDetection instances,
 * addressed by string IDs. Each instance can be single-model or multi-model.
 *
 * Thread-safety: public mutating methods synchronize on the per-instance object
 * and the manager keeps instances in a ConcurrentHashMap.
 */
public class KeywordDetectionAPI {

    private static final String TAG = "KeywordDetectionAPI";

    /** Listener for keyword detections (fired when detected==true). */
    public interface OnKeywordDetectionListener {
        /**
         * Called when a keyword is detected.
         * NOTE: This may run on a non-UI thread. If updating UI, post to the main thread.
         *
         * @param instanceId The instance that detected the phrase.
         * @param phrase     The detected phrase/model identifier (same as modelName from the callback).
         */
        void onKeywordDetected(@NonNull String instanceId, @NonNull String phrase);
    }

    /** Convenient configuration for building multi-model instances. */
    public static final class InstanceConfig {
        public final String modelName;          // path/identifier of the model (e.g., "hey_lookdeep.dm")
        public final float  threshold;          // 0..1
        public final int    bufferCnt;          // frames to aggregate pre/post
        public final boolean sticky;            // not used by KeyWordsDetection constructor; kept for parity
        public final long   msBetweenCallbacks; // debounce between callbacks per model

        public InstanceConfig(@NonNull String modelName,
                              float threshold,
                              int bufferCnt,
                              boolean sticky,
                              long msBetweenCallbacks) {
            this.modelName = modelName;
            this.threshold = threshold;
            this.bufferCnt = bufferCnt;
            this.sticky = sticky;
            this.msBetweenCallbacks = msBetweenCallbacks;
        }
    }

    private final Context appContext;
    private final Map<String, KeyWordsDetection> instances = new ConcurrentHashMap<>();
    private volatile @Nullable OnKeywordDetectionListener globalListener;

    /**
     * @param context Any context; the ApplicationContext is retained internally.
     */
    public KeywordDetectionAPI(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** Optional: Set one global listener to receive detection events from ALL instances. */
    public void setOnKeywordDetectionListener(@Nullable OnKeywordDetectionListener listener) {
        this.globalListener = listener;
    }

    /** Returns true if an instance with the given ID exists. */
    public boolean hasInstance(@NonNull String instanceId) {
        return instances.containsKey(instanceId);
    }

    /** Returns an immutable snapshot of current instance IDs. */
    @NonNull
    public List<String> listInstanceIds() {
        return Collections.unmodifiableList(new ArrayList<>(instances.keySet()));
    }

    /**
     * Create a SINGLE-model instance.
     *
     * @throws IllegalStateException if instanceId already exists
     * @throws Exception             if underlying creation/initialize fails
     */
    public void createInstance(@NonNull String instanceId,
                               @NonNull String modelName,
                               float threshold,
                               int bufferCnt) throws Exception {
        if (instances.containsKey(instanceId)) {
            throw new IllegalStateException("Instance already exists: " + instanceId);
        }

        KeyWordsDetection detector = new KeyWordsDetection(appContext, modelName, threshold, bufferCnt);
        // Bridge library callback -> API listener
        detector.initialize((detected, modelFromCallback) -> {
            if (detected) {
                OnKeywordDetectionListener l = globalListener;
                if (l != null) l.onKeywordDetected(instanceId, modelFromCallback);
            }
        });

        instances.put(instanceId, detector);
        Log.d(TAG, "Created single-model instance: " + instanceId + " (" + modelName + ")");
    }

    /**
     * Create a MULTI-model instance using parallel arrays.
     * All arrays must be the same length (>0).
     *
     * @throws IllegalArgumentException if array sizes mismatch or empty
     * @throws IllegalStateException    if instanceId already exists
     * @throws Exception                if underlying creation/initialize fails
     */
    public void createInstanceMulti(@NonNull String instanceId,
                                    @NonNull String[] modelPaths,
                                    @NonNull float[] thresholds,
                                    @NonNull int[] bufferCnts,
                                    @NonNull long[] msBetweenCallback) throws Exception {
        if (instances.containsKey(instanceId)) {
            throw new IllegalStateException("Instance already exists: " + instanceId);
        }
        int size = modelPaths.length;
        if (size == 0 ||
            thresholds.length != size ||
            bufferCnts.length != size ||
            msBetweenCallback.length != size) {
            throw new IllegalArgumentException("All input arrays must have the same non-zero length.");
        }

        KeyWordsDetection detector = new KeyWordsDetection(appContext, modelPaths, thresholds, bufferCnts, msBetweenCallback);
        detector.initialize((detected, modelFromCallback) -> {
            if (detected) {
                OnKeywordDetectionListener l = globalListener;
                if (l != null) l.onKeywordDetected(instanceId, modelFromCallback);
            }
        });

        instances.put(instanceId, detector);
        Log.d(TAG, "Created multi-model instance: " + instanceId + " (models=" + size + ")");
    }

    /**
     * Convenience: Create a MULTI-model instance from a list of {@link InstanceConfig}.
     */
    public void createInstanceMulti(@NonNull String instanceId,
                                    @NonNull List<InstanceConfig> configs) throws Exception {
        if (configs.isEmpty()) {
            throw new IllegalArgumentException("configs must not be empty");
        }
        String[] paths = new String[configs.size()];
        float[] thresholds = new float[configs.size()];
        int[] bufferCnts = new int[configs.size()];
        long[] msBetween = new long[configs.size()];
        for (int i = 0; i < configs.size(); i++) {
            InstanceConfig c = configs.get(i);
            paths[i] = c.modelName;
            thresholds[i] = c.threshold;
            bufferCnts[i] = c.bufferCnt;
            msBetween[i] = c.msBetweenCallbacks;
        }
        createInstanceMulti(instanceId, paths, thresholds, bufferCnts, msBetween);
    }

    /**
     * Replace (hot-swap) the model for an existing instance (single-model semantics).
     *
     * @throws IllegalStateException if the instance does not exist
     * @throws Exception             if underlying replace fails
     */
    public void replaceKeywordDetectionModel(@NonNull String instanceId,
                                             @NonNull String modelName,
                                             float threshold,
                                             int bufferCnt) throws Exception {
        KeyWordsDetection detector = requireInstance(instanceId);
        synchronized (detector) {
            detector.replaceKeywordDetectionModel(appContext, modelName, threshold, bufferCnt);
            Log.d(TAG, "Replaced model for instance " + instanceId + " -> " + modelName);
        }
    }

    /**
     * Set license key for an instance.
     *
     * @return true if licensed, false otherwise
     * @throws IllegalStateException if instance does not exist
     */
    public boolean setKeywordDetectionLicense(@NonNull String instanceId,
                                              @NonNull String licenseKey) {
        KeyWordsDetection detector = requireInstance(instanceId);
        synchronized (detector) {
            boolean ok = detector.setLicenseKey(licenseKey);
            Log.d(TAG, "License for " + instanceId + ": " + (ok ? "Licensed" : "Not Licensed"));
            return ok;
        }
    }

    /**
     * Start foreground service for an instance (if your library exposes this).
     *
     * @throws IllegalStateException if instance does not exist
     */
    public void startForegroundService(@NonNull String instanceId) {
        KeyWordsDetection detector = requireInstance(instanceId);
        synchronized (detector) {
            detector.startForegroundService();
            Log.d(TAG, "startForegroundService: " + instanceId);
        }
    }

    /**
     * Stop foreground service for an instance.
     *
     * @throws IllegalStateException if instance does not exist
     */
    public void stopForegroundService(@NonNull String instanceId) {
        KeyWordsDetection detector = requireInstance(instanceId);
        synchronized (detector) {
            detector.stopForegroundService();
            Log.d(TAG, "stopForegroundService: " + instanceId);
        }
    }

    /**
     * Start keyword detection with a threshold override.
     *
     * @throws IllegalStateException if instance does not exist
     * @throws OrtException          if thrown by underlying ORT calls
     */
    public void startKeywordDetection(@NonNull String instanceId, float threshold) throws OrtException {
        KeyWordsDetection detector = requireInstance(instanceId);
        synchronized (detector) {
            detector.startListening(threshold);
            Log.d(TAG, "Started detection: " + instanceId + " (threshold=" + threshold + ")");
        }
    }

    /**
     * Stop keyword detection.
     *
     * @throws IllegalStateException if instance does not exist
     */
    public void stopKeywordDetection(@NonNull String instanceId) {
        KeyWordsDetection detector = requireInstance(instanceId);
        synchronized (detector) {
            detector.stopListening();
            Log.d(TAG, "Stopped detection: " + instanceId);
        }
    }

    /**
     * Fetch the internal recording WAV path/string for an instance.
     *
     * @return recording WAV string (implementation-defined by your library)
     * @throws IllegalStateException if instance does not exist
     */
    @NonNull
    public String getRecordingWav(@NonNull String instanceId) throws Exception {
        KeyWordsDetection detector = requireInstance(instanceId);
        synchronized (detector) {
            return detector.getRecordingWav();
        }
    }

    /**
     * Destroy an instance and stop listening if active.
     *
     * @throws IllegalStateException if instance does not exist
     */
    public void destroyInstance(@NonNull String instanceId) {
        KeyWordsDetection detector = instances.remove(instanceId);
        if (detector == null) {
            throw new IllegalStateException("No instance found with ID: " + instanceId);
        }
        synchronized (detector) {
            try {
                detector.stopListening();
            } catch (Throwable ignored) {
                // no-op
            }
        }
        Log.d(TAG, "Destroyed instance: " + instanceId);
    }

    /** Destroy all instances (best-effort). */
    public void destroyAll() {
        for (String id : new ArrayList<>(instances.keySet())) {
            try {
                destroyInstance(id);
            } catch (Throwable t) {
                Log.w(TAG, "destroyAll() failed for " + id + ": " + t.getMessage());
            }
        }
    }

    // --------------------------
    // Internal helpers
    // --------------------------
    private KeyWordsDetection requireInstance(@NonNull String instanceId) {
        KeyWordsDetection d = instances.get(instanceId);
        if (d == null) {
            throw new IllegalStateException("Instance not found: " + instanceId);
        }
        return d;
    }
}

/*
USAGE GUIDE (mirrors your JS flow, but for a native Android app)

---------------------------------------
1) Initialize the API (keep one per process)
---------------------------------------
final KeywordDetectionAPI api = new KeywordDetectionAPI(appContext);

// Optional: register one global listener for ALL instances
api.setOnKeywordDetectionListener((instanceId, phrase) -> {
    // NOTE: This may be invoked on a background thread.
    // If you need to touch UI, post to the main thread.
    Log.d("KWDemo", "Detected on " + instanceId + ": " + phrase);
});

---------------------------------------
2) Build your instance configuration(s)
---------------------------------------
// Example: three models in ONE "multi_model_instance"
List<KeywordDetectionAPI.InstanceConfig> configs = new ArrayList<>();
configs.add(new KeywordDetectionAPI.InstanceConfig(
        "hey_lookdeep.dm", // change to the dm file you got from davoice
        0.99f, // Threshold
        4,     // Buffer_cnt
        false,     // sticky (kept for parity; the library constructor doesn't use it)
        1000L      // msBetweenCallbacks (debounce between repeated detections)
));
configs.add(new KeywordDetectionAPI.InstanceConfig("need_help_now.dm", 0.99f, 4, false, 1000L));
configs.add(new KeywordDetectionAPI.InstanceConfig("coca_cola_model_28_05052025.dm", 0.99f, 4, false, 1000L));

---------------------------------------
3) Create the instance (multi-model or single-model)
---------------------------------------
// MULTI-model:
api.createInstanceMulti("multi_model_instance", configs);

// Or SINGLE-model:
api.createInstance("single_model_instance", "hey_lookdeep.dm", 0.99f, 4);

---------------------------------------
4) (Optional) Start a foreground service for the instance
---------------------------------------
api.startForegroundService("multi_model_instance");
// ... later you can stop it with:
api.stopForegroundService("multi_model_instance");

---------------------------------------
5) Set license before starting detection
---------------------------------------
boolean licensed = api.setKeywordDetectionLicense("multi_model_instance",
        "MTc1Nzg4MzYwMDAwMA==-lULiXsf2XwqYXN5iJ8XddZTWT/r0T14dWX6zhyWGGO4=");
if (!licensed) {
    // handle unlicensed state (disable start, show UI, etc.)
}

---------------------------------------
6) Start detection
---------------------------------------
api.startKeywordDetection("multi_model_instance", 0.99f);

---------------------------------------
7) Receive detections
---------------------------------------
Your global OnKeywordDetectionListener above will be called with:
    instanceId = "multi_model_instance"
    phrase     = model name that fired (e.g., "hey_lookdeep.dm")

---------------------------------------
8) (Optional) Access the recording WAV
---------------------------------------
String wavInfoOrPath = api.getRecordingWav("multi_model_instance");
Log.d("KWDemo", "recordingWav=" + wavInfoOrPath);

---------------------------------------
9) Stop detection
---------------------------------------
api.stopKeywordDetection("multi_model_instance");

---------------------------------------
10) Swap models at runtime (single-model semantics)
---------------------------------------
api.replaceKeywordDetectionModel("multi_model_instance",
        "another_model.dm",
        0.98f,
        4);

---------------------------------------
11) Destroy when done (or app shutdown)
---------------------------------------
api.destroyInstance("multi_model_instance");
// Or nuke everything:
api.destroyAll();

NOTES:
- The listener is GLOBAL for simplicity. You get the instanceId for routing.
  If you need per-instance listeners, you can easily extend this class to store
  a Map<instanceId, OnKeywordDetectionListener> in parallel.
- Callbacks may arrive on a background thread. Post to main if touching UI.
- The “sticky” flag in InstanceConfig is carried over from your TS interface for parity,
  but your current library constructor doesn’t consume it. If you need it, wire it into
  your library and add a corresponding parameter or setter.
- Threshold in startKeywordDetection(...) lets you override at runtime.
- Foreground service calls are pass-throughs to your library methods.
*/
