package com.gammaplay.findmyphone.utils.service;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.musicg.api.ClapApi;
import com.musicg.wave.WaveHeader;

import java.util.LinkedList;

public class DetectorThread extends Thread {
    private volatile Thread _thread;
    private int numClaps;
    private OnSignalsDetectedListener onSignalsDetectedListener;
    private RecorderThread recorder;
    private WaveHeader waveHeader;
    private ClapApi clapApi;
    private int clapCheckLength = 1;
    private int clapPassScore = 1;
    private LinkedList<Boolean> clapResultList = new LinkedList<>();
    private String clapValue;

    public DetectorThread(RecorderThread recorderThread, String value) {
        this.clapValue = value;
        Log.d("DetectorThread", "Clap value set to: " + value);

        this.recorder = recorderThread;
        AudioRecord audioRecord = recorderThread.getAudioRecord();
        int bitsPerSample = (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? 16 : (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) ? 8 : 0;
        int channels = (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO) ? 1 : 0; // Assuming mono channel
        this.waveHeader = new WaveHeader();
        this.waveHeader.setChannels(channels);
        this.waveHeader.setBitsPerSample(bitsPerSample);
        this.waveHeader.setSampleRate(audioRecord.getSampleRate());
        this.clapApi = new ClapApi(this.waveHeader);
    }

    private void initBuffer() {
        this.numClaps = 0;
        this.clapResultList.clear();
        for (int i = 0; i < this.clapCheckLength; i++) {
            this.clapResultList.add(false);
        }
    }

    public void start() {
        this._thread = new Thread(this);
        this._thread.start();
    }

    public void stopDetection() {
        this._thread = null;
    }

    public void run() {
        try {
            initBuffer();
            Thread currentThread = Thread.currentThread();
            while (this._thread == currentThread) {
                byte[] frameBytes = this.recorder.getFrameBytes();
                if (frameBytes != null) {
                    boolean isClap = this.clapApi.isClap(frameBytes);
                    // Update clap result list and numClaps
                    if (this.clapResultList.getFirst()) {
                        this.numClaps--;
                    }
                    this.clapResultList.removeFirst();
                    this.clapResultList.add(isClap);

                    if (isClap) {
                        this.numClaps++;
                        Log.d("DetectorThread", "Clap detected, numClaps: " + this.numClaps);
                    }

                    // Check for single and double claps
                    if (this.numClaps == 1) {
                        // Single clap detected
                        Log.e("DetectorThread", "Single Clap Detected");
                        onSingleClapDetected(); // Notify listeners of single clap detection
                    } else if (this.numClaps >= this.clapPassScore) {
                        // Double clap detected
                        Log.e("DetectorThread", "Double Clap Detected");
                        initBuffer(); // Reset the buffer for the next detection
                        if (clapValue.equals("YES")) {
                            onClapDetected(); // Notify listeners of clap detection
                        }
                    }
                } else {
                    // If no frame is detected, update the result list
                    if (this.clapResultList.getFirst()) {
                        this.numClaps--;
                    }
                    this.clapResultList.removeFirst();
                    this.clapResultList.add(false); // Adding false for no detection
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DetectorThread", "Error in detection: " + e.getMessage());
        }
    }

    private void onSingleClapDetected() {
        Log.d("DetectorThread", "onSingleClapDetected triggered.");
        if (onSignalsDetectedListener != null) {
            onSignalsDetectedListener.onClapDetected(); // Notify listeners of single clap
        }
    }

    private void onClapDetected() {
        Log.d("DetectorThread", "onClapDetected triggered.");
        if (onSignalsDetectedListener != null) {
            onSignalsDetectedListener.onClapDetected(); // Notify listeners of clap detection
        }
    }

    public void setOnSignalsDetectedListener(OnSignalsDetectedListener onSignalsDetectedListener) {
        this.onSignalsDetectedListener = onSignalsDetectedListener;
    }

    public void interrupt() {
        super.interrupt();
    }
}
