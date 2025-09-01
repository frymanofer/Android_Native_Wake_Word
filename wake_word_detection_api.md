# WakeWordDetectionAPI Documentation

## Overview

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
        "hey_lookdeep.dm",
        0.99f,
        4,
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

---

## Dependencies
- `com.davoice.keywordsdetection.keywordslibrary.KeyWordsDetection`

---

## Notes
- Ensure that the required models and licenses are correctly configured for wake word, hotword, or phrase detection.
- Use proper error handling when interacting with the API methods.

