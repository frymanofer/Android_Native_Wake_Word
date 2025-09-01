# Android Native "wake word" by Davoice

## Android Native "wake word", "hotword", and "phrase spotting”

By [DaVoice.io](https://davoice.io)

[![Twitter URL](https://img.shields.io/twitter/url?style=social&url=https%3A%2F%2Ftwitter.com%2FDaVoiceAI)](https://twitter.com/DaVoiceAI)

Welcome to **Davoice Android Native Wake Word / hotword / Keywords Detection** – Wake words and keyword detection solution designed by **DaVoice.io**.

## About this package:

**The first Wake Word detection with optimized cross compiled dm libraries.**

### **Achieving battery consumption:**
- **0.02% per minute** 
- **1.2% per hour** 
- **83 hours constantly listening and analyzing multiple wake words** 

This is an optimized **"wake word"** package for Android Native. A "wake word" is a keyword that activates your device, like "Hey Siri" or "OK Google". "Wake Word" is also known as "keyword detection", "Phrase Recognition", "Phrase Spotting", “Voice triggered”, “hot word”, “trigger word”

It also provide **Speech to Intent**. **Speech to Intent** refers to the ability to recognize a spoken word or phrase
and directly associate it with a specific action or operation within an application. Unlike a **"wake word"**, which typically serves to activate or wake up the application,
Speech to Intent goes further by enabling complex interactions and functionalities based on the recognized intent behind the speech.

For example, a wake word like "Hey App" might activate the application, while Speech
to Intent could process a phrase like "Play my favorite song" or "Order a coffee" to
execute corresponding tasks within the app.
Speech to Intent is often triggered after a wake word activates the app, making it a key
component of more advanced voice-controlled applications. This layered approach allows for
seamless and intuitive voice-driven user experiences.

More questions? - Contact us at info@DaVoice.io

## Features

- **High Accuracy:** We have successfully reached over 99% accuracy for all our models. **Here is on of our customer's benchmarks**:

```
MODEL         DETECTION RATE
===========================
DaVoice        0.992481
Top Player     0.874812
Third          0.626567
```

- **Achieving extraordinary battery life time:**
    - **0.02% per minute** 
    - **1.2% per hour** 
    - **83 hours constantly listening and analyzing multiple wake words** 

- **Low Latency:** Experience near-instantaneous keyword detection.

## Platforms and Supported Languages

- **Android wake word:** API for Android Native.

# Wake word generator

## Create your "custom wake word""

In order to generate your custom wake word you will need to:

- **Create wake word mode:**
    Contact us at info@davoice.io with a list of your desired **"custom wake words"**.

    We will send you corresponding models typically your **wake word phrase .dm** for example:

    A wake word ***"hey sky"** will correspond to **hey_sky.dm**.

- **Add wake words to Android:**
    Simply copy the new dm files to:

    android/app/src/main/assets/*.dm

- **Call the WakeWordDetectionAPI with the new dm file:**

```
createInstance(instanceId, **"hey_sky.dm"**,  threshold,  bufferCnt);
```

- **Last step - Rebuild your project**

## Contact

For any questions, requirements, or more support for Android Native, please contact us at info@davoice.io.

## Android:
Add this to your android/build.gradle file:

```
    maven {
        url './libs'
    }
    maven {
        url "$projectDir/libs"
    }
    mavenLocal()
```

And this to either your android/build.gradle or android/app/build.gradle depending if you are building a library or an App:

```
   dependencies {
      implementation 'ai.picovoice:android-voice-processor:1.0.2'
      implementation 'com.davoice:keyworddetection:1.0.0'
   }
```

# WakeWordDetectionAPI Documentation

## Overview
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


## Githhub examples:

### Checkout examples on:
Coming soon...

## FAQ:

### What is a wake word?

A **"wake word"** is a keyword or phrase that activates your device, like "Hey Siri" or "OK Google". "Wake Word" is also known as "keyword detection", "Phrase Recognition", "Phrase Spotting", “Voice triggered”, “hot word”, “trigger word”...

### What is a Speech to Intent?

**"Speech to Intent"** refers to the ability to recognize a spoken word or phrase
and directly associate it with a specific action or operation within an application.

Unlike a **"wake word"**, which typically serves to activate or wake up the application,
Speech to Intent goes further by enabling complex interactions and functionalities
based on the recognized intent behind the speech.

For example, a wake word like "Hey App" might activate the application, while Speech
to Intent could process a phrase like "Play my favorite song" or "Order a coffee" to
execute corresponding tasks within the app.
Speech to Intent is often triggered after a wake word activates the app, making it a key
component of more advanced voice-controlled applications. This layered approach allows for
seamless and intuitive voice-driven user experiences.

### How accurate is the platform?

We have successfully reached over 99% "wake word" accuracy for all our models. **Here is on of our customer's benchmarks**:

```
MODEL         DETECTION RATE
===========================
DaVoice        0.992481
Top Player     0.874812
Third          0.626567
```

### Key words

"DaVoice.io" 
"Voice commands"
"Wake word detection github"
“Voice triggered”
“hot word”
“Android trigger word”
“Android Voice triggered”
“Android hot word”
"Android wake word",
"Wake word generator",
"hot word generator",
"trigger word generator",
"Custom wake word generator",
"Custom hot word",
"Custom trigger word",
"Custom wake word",
"voice commands",
"wake word",
"wakeword",
"wake words",
"keyword detection",
"keyword spotting",
"speech to intent",
"voice commands",
"voice to intent",
"phrase spotting",
"Android wake word",
"Davoice.io wake word",
"Davoice wake word",
"Davoice Android wake word",
"Davoice Android wake word",
"wake",
"word",
"Voice Commands Recognition",
"lightweight Voice Commands Recognition",
"customized lightweight Voice Commands Recognition",
"rn wake word"
"Davoice.io",
"voice commands",
"wake word",
"wakeword",
"wake words",
"keyword detection",
"keyword spotting",
"Wake word detection github"
"Wake Word" 
"keyword detection"
"Phrase Recognition"
"Phrase Spotting"
"Android wake word",
"Custom wake word",
"voice commands",
"wake word",
"wakeword",
"wake words",
"keyword detection",
"keyword spotting",
"speech to intent",
"voice to intent",
"phrase spotting",
"Android wake word",
"Davoice.io wake word",
"Davoice wake word",
"Davoice wake word",
"Davoice Android wake word",
"Davoice.io Android wake word",
"wake",
"word",
"Voice Commands Recognition",
"lightweight Voice Commands Recognition",
"customized lightweight Voice Commands Recognition",
"rn wake word"
"speech to intent",
"voice to intent",
"phrase spotting",
"Android wake word",
"Davoice.io wake word",
"Davoice wake word",
"Davoice Android wake word",
"Davoice Android wake word",
"wake",
"word",
"Voice Commands Recognition",
"lightweight Voice Commands Recognition",
"customized lightweight Voice Commands Recognition",
"Custom wake word",
"rn wake word"



