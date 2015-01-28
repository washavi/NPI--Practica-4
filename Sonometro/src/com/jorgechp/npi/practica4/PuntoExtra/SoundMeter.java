package com.jorgechp.npi.practica4.PuntoExtra;

import java.io.IOException;

import android.media.MediaRecorder;
//http://stackoverflow.com/questions/14181449/android-detect-sound-level
public class SoundMeter {

    private MediaRecorder mRecorder = null;

    public void start() throws IllegalStateException, IOException {
            if (mRecorder == null) {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mRecorder.setOutputFile("/dev/null"); 
                    mRecorder.prepare();
                    mRecorder.start();
            }
    }

    public void stop() {
            if (mRecorder != null) {
                    mRecorder.stop();       
                    mRecorder.release();
                    mRecorder = null;
            }
    }

    public double getAmplitude() {
            if (mRecorder != null)
                    return  mRecorder.getMaxAmplitude();
            else
                    return 0;

    }
}