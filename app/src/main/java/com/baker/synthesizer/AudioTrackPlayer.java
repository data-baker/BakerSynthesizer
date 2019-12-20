package com.baker.synthesizer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Create by hsj55
 * 2019/11/27
 */
public class AudioTrackPlayer {
    private static String TAG = "AudioTrackPlayer";
    private final int SAMPLE_RATE = 16000;
    private boolean playing = false;
    private LinkedBlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>();

    // 初始化播放器
    private int iMinBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    private AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO
            , AudioFormat.ENCODING_PCM_16BIT,
            iMinBufSize * 10, AudioTrack.MODE_STREAM);
    private byte[] tempData;

    private Thread ttsPlayerThread;

    AudioTrackPlayer() {
        Log.i(TAG, "init");
        if (tempData != null) {
            tempData = null;
        }
        playing = true;
        ttsPlayerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (playing) {
                    tempData = audioQueue.poll();
                    if (tempData == null) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                            Log.d(TAG, "audioTrack.play");
                            audioTrack.play();
                        }
                        Log.d(TAG, "audioTrack.write");
                        audioTrack.write(tempData, 0, tempData.length);
                    }
                }
                Log.d(TAG, "playing thread end");
            }
        });
        ttsPlayerThread.start();
    }

    public void cleanAudioData() {
        audioQueue.clear();
    }

    public void setAudioData(byte[] data) {
        audioQueue.offer(data);
    }

    public void stop() {
        playing = false;
        audioTrack.pause();
        audioQueue.clear();
        audioTrack.flush();
        audioTrack.stop();
        Log.d(TAG, "stopped");
    }
}
