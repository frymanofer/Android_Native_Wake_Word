# Android Native "wake word" by Davoice

## Android Native "wake word", "hotword", and "phrase spotting”

By [DaVoice.io](https://davoice.io)

[![Twitter URL](https://img.shields.io/twitter/url?style=social&url=https%3A%2F%2Ftwitter.com%2FDaVoiceAI)](https://twitter.com/DaVoiceAI)

Welcome to **Davoice Android Native Wake Word / hotword / Keywords Detection** – Wake words and keyword detection solution designed by **DaVoice.io**.

## About this package:

**The first Wake Word detection with optimized cross compiled onnx libraries.**

### **Achieving battery consumption:**
- **0.02% per minute** 
- **1.2% per hour** 
- **83 hours constantly listening and analyzing multiple wake words** 

This is a **"wake word"** package for React Native. A "wake word" is a keyword that activates your device, like "Hey Siri" or "OK Google". "Wake Word" is also known as "keyword detection", "Phrase Recognition", "Phrase Spotting", “Voice triggered”, “hot word”, “trigger word”

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

- **High Accuracy:** We have succesfully reached over 99% accurary for all our models. **Here is on of our customer's benchmarks**:

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

    We will send you corresponding models typically your **wake word phrase .onnx** for example:

    A wake word ***"hey sky"** will correspond to **hey_sky.onnx**.

- **Add wake words to Android:**
    Simply copy the new onnx files to:

    android/app/src/main/assets/*.onnx

- **Call the WakeWordDetectionAPI with the new onnx file:**

```
createInstance(instanceId, **"hey_sky.onnx"**,  threshold,  bufferCnt);
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
The `WakeWordDetectionAPI` class provides functionality for managing instances of "wake word" detection, also known as "keyword detection", "Phrase Recognition", "Phrase Spotting", “Voice triggered”, “hot word” and “trigger word”, interacting with the `KeyWordsDetection` library. It allows you to create and manage multiple instances, start and stop wake word detection, and manage foreground services to use 'Wake word', "hotword" or "phrase detection" in the background.

---

## Constructor
### `WakeWordDetectionAPI(Context context)`
Initializes a new instance of the API with the provided Android `Context`.

#### Parameters:
- `context` *(Context)*: The Android context, typically an `Application` or `Activity` context.

---

## Methods

### `boolean setWakeWordDetectionLicense(String instanceId, String licenseKey)`
Sets the license key for a specific wake word detection instance.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.
- `licenseKey` *(String)*: The license key to be set.

#### Returns:
- `true` if the license was successfully set.
- `false` if the instance was not found or the license could not be set.

---

### `boolean createInstance(String instanceId, String modelName, float threshold, int bufferCnt)`
Creates a new wake word detection instance with the specified parameters.

#### Parameters:
- `instanceId` *(String)*: Unique identifier for the instance.
- `modelName` *(String)*: The name of the wake word detection model to be used.
- `threshold` *(float)*: The detection threshold.
- `bufferCnt` *(int)*: This specifies the number of times the speech buffer is rechecked using slightly adjusted models. It significantly reduces false positives to near zero.

#### Returns:
- `true` if the instance was successfully created.
- `false` if an error occurred or the instance already exists.

---

### `String getRecordingWav(String instanceId)`
Retrieves the recording WAV file for a specific wake word detection instance.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.

#### Returns:
- The path to the recording WAV file, or `null` if the instance does not exist or an error occurred.

---

### `boolean replaceWakeWordDetectionModel(String instanceId, String modelName, float threshold, int bufferCnt)`
Replaces the wake word detection model for an existing instance.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.
- `modelName` *(String)*: The name of the new wake word or hotword model.
- `threshold` *(float)*: The new detection threshold.
- `bufferCnt` *(int)*: The new buffer count.

#### Returns:
- `true` if the model was successfully replaced.
- `false` if the instance was not found or an error occurred.

---

### `boolean startWakeWordDetection(String instanceId, float threshold)`
Starts wake word or hotword detection for a specific instance.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.
- `threshold` *(float)*: The detection threshold.

#### Returns:
- `true` if detection was successfully started.
- `false` if the instance was not found or an error occurred.

---

### `boolean stopWakeWordDetection(String instanceId)`
Stops wake word or phrase detection for a specific instance.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.

#### Returns:
- `true` if detection was successfully stopped.
- `false` if the instance was not found or an error occurred.

---

### `boolean startForegroundService(String instanceId)`
Starts a foreground service for a specific wake word detection instance.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.

#### Returns:
- `true` if the service was successfully started.
- `false` if the instance was not found or an error occurred.

---

### `boolean stopForegroundService(String instanceId)`
Stops a foreground service for a specific wake word detection instance.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.

#### Returns:
- `true` if the service was successfully stopped.
- `false` if the instance was not found or an error occurred.

---

### `boolean destroyInstance(String instanceId)`
Destroys a wake word detection instance, stopping any ongoing detection and cleaning up resources.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.

#### Returns:
- `true` if the instance was successfully destroyed.
- `false` if the instance was not found.

---

## Private Methods

### `void onWakeWordDetected(String instanceId, Boolean detected)`
Handles the wake word detection event for a specific instance.

#### Parameters:
- `instanceId` *(String)*: The ID of the instance.
- `detected` *(Boolean)*: Whether the wake word or phrase was detected.

---

## Logging
The `WakeWordDetectionAPI` logs important actions and errors using Android's `Log` class with the tag `KeyWordsDetection`.

---

## Example Usage
```java
// Initialize the API
WakeWordDetectionAPI api = new WakeWordDetectionAPI(context);

// Create an instance
boolean created = api.createInstance("instance1", "wakeword_model.onnx", 0.99f, 10);

// Set a license key
boolean licensed = api.setWakeWordDetectionLicense("instance1", "your-license-key");

// Start wake word detection
boolean started = api.startWakeWordDetection("instance1", 0.99f);

// Stop wake word detection
boolean stopped = api.stopWakeWordDetection("instance1");

// Destroy the instance
boolean destroyed = api.destroyInstance("instance1");
```

---

## Dependencies
- `com.davoice.keywordsdetection.keywordslibrary.KeyWordsDetection`

---

## Notes
- Ensure that the required models and licenses are correctly configured for wake word, hotword, or phrase detection.
- Use proper error handling when interacting with the API methods.


## Githhub examples:

### Checkout examples on:
https://github.com/frymanofer/ReactNative_WakeWordDetection

https://github.com/frymanofer/ReactNative_WakeWordDetection/example_npm

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

We have succesfully reached over 99% "wake word" accurary for all our models. **Here is on of our customer's benchmarks**:

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
“react-native trigger word”
“react-native Voice triggered”
“react-native hot word”
"react-native wake word",
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
"react native wake word",
"Davoice.io wake word",
"Davoice wake word",
"Davoice react native wake word",
"Davoice react-native wake word",
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
"react-native wake word",
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
"react native wake word",
"Davoice.io wake word",
"Davoice wake word",
"Davoice wake word",
"Davoice react native wake word",
"Davoice.io react-native wake word",
"wake",
"word",
"Voice Commands Recognition",
"lightweight Voice Commands Recognition",
"customized lightweight Voice Commands Recognition",
"rn wake word"
"speech to intent",
"voice to intent",
"phrase spotting",
"react native wake word",
"Davoice.io wake word",
"Davoice wake word",
"Davoice react native wake word",
"Davoice react-native wake word",
"wake",
"word",
"Voice Commands Recognition",
"lightweight Voice Commands Recognition",
"customized lightweight Voice Commands Recognition",
"Custom wake word",
"rn wake word"



