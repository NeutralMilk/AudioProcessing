package com.example.colin.audioprocessing;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static android.content.Context.MODE_APPEND;

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
    private static final int WINDOW_SIZE_AMP    = 1024;
    private static final int WINDOW_OVERLAP_PITCH = WINDOW_SIZE_PITCH * 3/4;
    private AudioRecord recorder    = null;
    private boolean isRecording = false;
    public int count;

    //General variables
    public boolean active;
    ProcessAmplitude pa;
    ProcessNote pn;
    String note;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        System.out.println("this works 134");
        startRecording();
    }

    //The startRecording method is adapted from https://github.com/solarus/CTuner/blob/master/src/org/tunna/ctuner/MainActivity.java
    public void startRecording()
    {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLERATE,
                NUM_CHANNELS,
                RECORDER_ENCODING,
                WINDOW_SIZE_PITCH);
        recorder.startRecording();
        isRecording = true;

        new Thread() {
            public void run()
            {
                final short[] sData = new short[WINDOW_SIZE_PITCH];
                final float[] fData = new float[WINDOW_SIZE_PITCH];

                final int diffPitch = WINDOW_SIZE_PITCH - WINDOW_OVERLAP_PITCH;

                // This loop will be correct after 3 rounds because of
                // the WINDOW_OVERLAP offset
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
                            pn = new ProcessNote();
                            note = pn.processNote(fData);
                        }
                    });

                    pa = new ProcessAmplitude();
                    double[] amplitude = pa.processAmplitude(sData);

                    //System.out.println("Note is " + note);

                    for(int i  = 0; i < 4; i ++)
                    {
                        System.out.println("A" + i + " is " + amplitude[i]);

                    }
                    for (int i = 0; i < WINDOW_OVERLAP_PITCH; ++i)
                    {
                        sData[i] = sData[i + diffPitch];
                        fData[i] = (float) sData[i + diffPitch];
                    }//end for*/

                    count++;

                    /*if(active)
                    {
                        if(count%4 == 0)
                        {
                            count = 0;
                            init();
                        }//end if
                    }*/
                }
            }
        }.start();
    }
}
