package com.example.colin.audioprocessing;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Colin on 27/03/2018.
 */

public class LiveProcessing extends AppCompatActivity
{

    //audio settings
    private static final int SAMPLERATE        = 44100;
    private static final int NUM_CHANNELS      = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int WINDOW_SIZE_PITCH    = 4096;
    private AudioRecord recorder    = null;
    private boolean isRecording = false;
    public int count;

    //General variables
    public boolean active;
    ProcessAmplitude pa;
    ProcessNote pn;
    Segmentation segment;
    String note;

    public void startRecording()
    {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLERATE,
                NUM_CHANNELS,
                RECORDER_ENCODING,
                WINDOW_SIZE_PITCH);
        recorder.startRecording();
        isRecording = true;

        pa = new ProcessAmplitude();
        pn = new ProcessNote();

        new Thread() {
            public void run()
            {
                final short[] sData = new short[1024];
                final float[] fData = new float[WINDOW_SIZE_PITCH];

                final int diffPitch = 2048;

                // This loop will be correct after 3 rounds because of
                // the WINDOW_OVERLAP offset
                while (isRecording)
                {
                    recorder.read(sData, 0, 1024);

                    for (int j = 0; j < 1024; j++)
                    {
                        fData[j] = (float) sData[j];
                        //ystem.out.println("fdata is" + fData[j]);
                    }//end for

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            note = pn.processNote(fData);
                        }
                    });


                    double amplitude = pa.processAmplitude(sData);

                    System.out.println("Note is " + note);
                    System.out.println("A" + " is " + amplitude);

                    segment = new Segmentation();
                    //boolean valid = segment.segmentation(note, amplitude);
                    count++;

                }
            }
        }.start();
    }
}
