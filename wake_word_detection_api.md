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

