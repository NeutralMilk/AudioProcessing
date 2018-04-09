package com.example.colin.audioprocessing;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
    private static final int WINDOW_OVERLAP_PITCH = WINDOW_SIZE_PITCH * 3/4;
    private AudioRecord recorder    = null;
    public boolean isRecording = false;
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
        segment = new Segmentation();


        new Thread() {
            public void run()
            {

                final short[] sData = new short[WINDOW_SIZE_PITCH];
                final float[] fData = new float[WINDOW_SIZE_PITCH];

                final int diffPitch = WINDOW_SIZE_PITCH - WINDOW_OVERLAP_PITCH;

                //first write diffByte amount of bytes into the WINDOW_OVERLAP_BYTES position of the buffer
                //this will fill the last 1/4 of the buffer
                //this will then each '1/4' segment of the buffer will be moved back 1/4
                //this allows me to fill a buffer of size 4096 but make readings 4 times faster
                while (isRecording)
                {
                    recorder.read(sData, WINDOW_OVERLAP_PITCH, diffPitch);

                    for (int j = WINDOW_OVERLAP_PITCH; j < diffPitch; ++j)
                    {
                        fData[j] = (float) sData[j];
                        Log.v(String.valueOf(fData[j]), "fdata");
                        System.out.println("fdata is" + fData[j]);
                    }//end for

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            note = pn.processNote(fData);
                        }
                    });


                    double amp = pa.processAmplitude(sData);

                    //System.out.println("Note is " + currentNote);
                    float valid = segment.segmentation(note, amp);

                    //this will move each quarter back one quarter
                    for (int i = 0; i < WINDOW_OVERLAP_PITCH; ++i)
                    {
                        sData[i] = sData[i + diffPitch];
                        fData[i] = (float) sData[i + diffPitch];
                    }//end for*/

                    count++;

                }

            }
        }.start();
    }
    public void stopRecording()
    {
        isRecording = false;
        recorder.stop();
        recorder.release();
        recorder = null;
    }
}
