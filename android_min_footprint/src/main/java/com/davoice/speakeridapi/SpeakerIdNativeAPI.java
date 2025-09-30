package com.davoice.speakeridapi;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.davoice.speakerid.*;


/** 
 * Usage:
 * 
 * 
    SpeakerIdNativeAPI sidMgr = new SpeakerIdNativeAPI(appContext);
 
    // create a WWD instance
    sidMgr.createInstanceWWD("sidA");
    // Or create a regular instance for none WWD use-case
    // sidMgr.createInstance("sidA");
    
    // external-audio cluster flow:
    int clusterId = sidMgr.initCluster("sidA", 3);
    sidMgr.createAndPushEmbeddingsToCluster("sidA", clusterId, pcmBlock, pcmBlock.length);
    float score = sidMgr.createAndVerifyEmbeddingsFromCluster("sidA", clusterId, verifyPcm, verifyPcm.length);
 
    // mic flows (permissions required):
    OnboardingResult ob = sidMgr.onboardFromMicrophoneWWD("sidA", 3, 12000);
    VerificationResult vr = sidMgr.verifyFromMicrophoneWWD("sidA", 6000);
*
*
**/

/**
 * SpeakerIdNativeAPI
 *
 * A lightweight, native (non-React) manager for multiple SpeakerIdApi instances,
 * addressed by string IDs. Includes external-audio cluster APIs (KWD-style buffers).
 *
 * Thread-safety: public mutating methods synchronize on the per-instance object.
 */
public class SpeakerIdNativeAPI {

    private static final String TAG = "SpeakerIdNativeAPI";

    private final Context appContext;
    private final Map<String, SpeakerIdApi> instances = new ConcurrentHashMap<>();
    private final Map<String, SpeakerIdApi.OnboardingStream> onboardingStreams = new ConcurrentHashMap<>();

    public SpeakerIdNativeAPI(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    // ---------------------------
    // Instance lifecycle
    // ---------------------------

    /** Returns true if an instance with the given ID exists. */
    public boolean hasInstance(@NonNull String instanceId) {
        return instances.containsKey(instanceId);
    }

    /** Returns an immutable snapshot of current instance IDs. */
    @NonNull
    public List<String> listInstanceIds() {
        return Collections.unmodifiableList(new ArrayList<>(instances.keySet()));
    }

    /** Create a standard SpeakerIdApi instance. */
    public void createInstance(@NonNull String instanceId) throws Exception {
        if (instances.containsKey(instanceId)) {
            throw new IllegalStateException("Instance already exists: " + instanceId);
        }
        SpeakerIdApi api = SpeakerIdApi.create(appContext);
        instances.put(instanceId, api);
        Log.d(TAG, "Created instance: " + instanceId);
    }

    /** Create a WWD-tuned SpeakerIdApi instance (uses separate config/files). */
    public void createInstanceWWD(@NonNull String instanceId) throws Exception {
        if (instances.containsKey(instanceId)) {
            throw new IllegalStateException("Instance already exists: " + instanceId);
        }
        SpeakerIdApi api = SpeakerIdApi.createWWD(appContext);
        instances.put(instanceId, api);
        Log.d(TAG, "Created WWD instance: " + instanceId);
    }

    /** Destroy an instance (best-effort). */
    public void destroyInstance(@NonNull String instanceId) {
        SpeakerIdApi api = instances.remove(instanceId);
        if (api == null) {
            throw new IllegalStateException("No instance found: " + instanceId);
        }
        synchronized (api) {
            try { api.close(); } catch (Throwable ignore) {}
        }
        onboardingStreams.remove(instanceId);
        Log.d(TAG, "Destroyed instance: " + instanceId);
    }

    /** Destroy all instances. */
    public void destroyAll() {
        for (String id : new ArrayList<>(instances.keySet())) {
            try { destroyInstance(id); } catch (Throwable t) {
                Log.w(TAG, "destroyAll() failed for " + id + ": " + t.getMessage());
            }
        }
    }

    // ---------------------------
    // Verification target init
    // ---------------------------

    /** Return true if default mean/cluster exist for this instance. */
    public boolean initVerificationUsingDefaults(@NonNull String instanceId) {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.initVerificationUsingDefaults(appContext);
        }
    }

    /** Point the instance at explicit mean/cluster files. */
    public boolean initVerificationWithFiles(@NonNull String instanceId,
                                             @NonNull File meanNpy,
                                             @NonNull File clusterNpy) {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.initVerificationWithFiles(meanNpy, clusterNpy);
        }
    }

    /** Wipe default on-disk targets and reset in-memory state. */
    public void wipeAllTargetsAndReset(@NonNull String instanceId) {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            api.wipeAllTargetsAndReset();
        }
    }

    // ---------------------------
    // Onboarding (MIC)
    // ---------------------------

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    public OnboardingResult onboardFromMicrophone(@NonNull String instanceId, long maxMillis) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.onboardFromMicrophone(maxMillis);
        }
    }

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    public OnboardingResult onboardFromMicrophoneUntil(@NonNull String instanceId,
                                                       float targetVoicedSec,
                                                       long hardTimeoutMs) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.onboardFromMicrophoneUntil(targetVoicedSec, hardTimeoutMs);
        }
    }

    // ---------------------------
    // Onboarding (STREAM)
    // ---------------------------

    /** Start a streaming onboarding session. */
    public void startOnboardingStream(@NonNull String instanceId) {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            SpeakerIdApi.OnboardingStream s = api.startOnboardingStream();
            onboardingStreams.put(instanceId, s);
        }
    }

    /** Feed a PCM16 block to the onboarding stream. Returns result when completed, else null. */
    @Nullable
    public OnboardingResult feedOnboardingStream(@NonNull String instanceId, @NonNull short[] pcm) throws Exception {
        SpeakerIdApi.OnboardingStream s = onboardingStreams.get(instanceId);
        if (s == null) throw new IllegalStateException("Stream not started for: " + instanceId);
        return s.feed(pcm);
    }

    /** Finish the onboarding stream (flush). */
    @Nullable
    public OnboardingResult finishOnboardingStream(@NonNull String instanceId) throws Exception {
        SpeakerIdApi.OnboardingStream s = onboardingStreams.remove(instanceId);
        if (s == null) throw new IllegalStateException("Stream not started for: " + instanceId);
        return s.finish();
    }

    // ---------------------------
    // Onboarding (WAV)
    // ---------------------------

    public OnboardingResult onboardFromWav(@NonNull String instanceId, @NonNull File wav) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.onboardFromWav(wav);
        }
    }

    // ---------------------------
    // Verification (MIC)
    // ---------------------------

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    public VerificationResult verifyFromMicrophone(@NonNull String instanceId, long maxMillis) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.verifyFromMicrophone(maxMillis);
        }
    }

    // ---------------------------
    // Verification (STREAM)
    // ---------------------------

    @Nullable
    public VerificationResult verifyStreamPush(@NonNull String instanceId, @NonNull short[] block) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.verifyStreamPush(block);
        }
    }

    @Nullable
    public VerificationResult verifyStreamFinish(@NonNull String instanceId) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.verifyStreamFinish();
        }
    }

    // ---------------------------
    // Verification (WAV)
    // ---------------------------

    public VerificationResult verifyFromWav(@NonNull String instanceId, @NonNull File wav) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.verifyFromWav(wav);
        }
    }

    // ---------------------------
    // WWD helpers
    // ---------------------------

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    public OnboardingResult onboardFromMicrophoneWWD(@NonNull String instanceId, int embNum, long maxWallMs) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.onboardFromMicrophoneWWD(embNum, maxWallMs);
        }
    }

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    public VerificationResult verifyFromMicrophoneWWD(@NonNull String instanceId, long maxWallMs) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.verifyFromMicrophoneWWD(maxWallMs);
        }
    }

    // ---------------------------
    // Export helpers
    // ---------------------------

    @NonNull
    public Uri exportDefaultClusterToDownloads(@NonNull String instanceId) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.exportDefaultClusterToDownloads(appContext);
        }
    }

    @NonNull
    public Uri exportDefaultMeanToDownloads(@NonNull String instanceId) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.exportDefaultMeanToDownloads(appContext);
        }
    }

    @NonNull
    public Uri exportDefaultMeanCountToDownloads(@NonNull String instanceId) throws Exception {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.exportDefaultMeanCountToDownloads(appContext);
        }
    }

    // ---------------------------
    // EXTERNAL-AUDIO CLUSTER API
    // (delegates to methods you added in SpeakerIdApi)
    // ---------------------------

    /** Create/restore a cluster (FIFO size = numOfEmb) and return its clusterId. */
    public int initCluster(@NonNull String instanceId, int numOfEmb) {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.initCluster(numOfEmb);
        }
    }

    /**
     * Create exactly ONE embedding from the given pcm buffer (prefers last 1.0 s, pads by duplication),
     * push it into the cluster (FIFO), and persist cluster+mean.
     */
    public void createAndPushEmbeddingsToCluster(@NonNull String instanceId,
                                                 int clusterId,
                                                 @NonNull short[] pcm,
                                                 int length) {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            api.createAndPushEmbeddingsToCluster(clusterId, pcm, length);
        }
    }

    /**
     * Create exactly ONE embedding from the given pcm buffer (same window/pad policy as push),
     * and return the best cosine score vs {mean ∪ cluster rows}.
     */
    public float createAndVerifyEmbeddingsFromCluster(@NonNull String instanceId,
                                                      int clusterId,
                                                      @NonNull short[] pcm,
                                                      int length) {
        SpeakerIdApi api = require(instanceId);
        synchronized (api) {
            return api.createAndVerifyEmbeddingsFromCluster(clusterId, pcm, length);
        }
    }

    // ---------------------------
    // Internal helper
    // ---------------------------

    private SpeakerIdApi require(@NonNull String instanceId) {
        SpeakerIdApi api = instances.get(instanceId);
        if (api == null) throw new IllegalStateException("Instance not found: " + instanceId);
        return api;
    }
}
