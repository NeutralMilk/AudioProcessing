package com.example.colin.audioprocessing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity
{

    //pitch settings
    private static final int SAMPLERATE        = 44100;
    private static final int NUM_CHANNELS      = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE    = 4096;
    private static final int BUFFER_OVERLAY = BUFFER_SIZE * 3/4;
    private static final int FRAMERATE    = 60;
    private static final int UPDATE_DELAY = 1000/FRAMERATE;
    private AudioRecord recorder    = null;
    private boolean isRecording = false;
    private Yin yin = null;
    private long lastUpdateTime = 0;

    //display
    TextView tvFreq;
    TextView tvNote;
    ToggleButton tb;

    //graph variables
    LineGraphSeries<DataPoint> xySeries;
    private ArrayList<XYValue> xyArray;
    GraphView fGraph;
    public float pitch;
    public float x;
    public int count;
    public boolean active;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        }//end if

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tvFreq = (TextView)findViewById(R.id.tvFreq);
        tvNote = (TextView)findViewById(R.id.tvNote);
        tb = (ToggleButton) findViewById(R.id.tb);

        count = 0;
        xyArray = new ArrayList<>();
        fGraph = (GraphView) findViewById(R.id.fGraph);
        x = 0;
        active = false;

        tb.setText("Record");
        tb.setTextOff("Record");
        tb.setTextOn("Stop");

        //graph settings
        fGraph.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        fGraph.getViewport().setScalable(true);
        fGraph.getViewport().setScrollable(true);
        fGraph.getViewport().setYAxisBoundsManual(true);
        fGraph.getViewport().setMaxY(2500);
        fGraph.getViewport().setMinY(0);
        fGraph.getViewport().setMinX(0);

        double yinThreshold = 0.3;
        yin = new Yin(SAMPLERATE, BUFFER_SIZE, yinThreshold);

    }

    private void startRecording() {
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLERATE, NUM_CHANNELS, RECORDER_ENCODING);

        final int bufferSize = Math.max(minBufferSize, BUFFER_SIZE);

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLERATE,
                NUM_CHANNELS,
                RECORDER_ENCODING,
                bufferSize);
        recorder.startRecording();
        isRecording = true;

        new Thread() {
            public void run() {
                final short[] sData = new short[BUFFER_SIZE];
                final float[] fData = new float[BUFFER_SIZE];

                final int diff = bufferSize - BUFFER_OVERLAY;

                // This loop will be correct after 3 rounds because of
                // the BUFFER_OVERLAY offset
                while (isRecording)
                {
                    recorder.read(sData, BUFFER_OVERLAY, diff);

                    for (int i = BUFFER_OVERLAY; i < diff; ++i) {
                        fData[i] = (float) sData[i];
                    }//end for

                    float currentPitch = yin.getPitch(fData).getPitch();
                    if (currentPitch != -1 && currentPitch > 30) {
                        pitch = currentPitch;
                    }//end if

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            updateNote(pitch);
                        }
                    });

                    for (int i = 0; i < BUFFER_OVERLAY; ++i) {
                        sData[i] = sData[i + diff];
                        fData[i] = (float) sData[i + diff];
                    }//end for
                }
            }
        }.start();
    }

    private synchronized void updateNote(final float pitch) {
        long currentTime = System.currentTimeMillis();
        if (lastUpdateTime < currentTime - UPDATE_DELAY)
        {

            convertToNote(pitch);
            lastUpdateTime = currentTime;

            count++;

            //limit to once every 10 readings, otherwise it fills memory too quickly and crashes.
            if(count%4 == 0)
            {
                count = 0;
                init();
            }//end if
        }
    }

    private void stopRecording() {
        isRecording = false;
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void init()
    {
        xySeries = new LineGraphSeries<>();
        float y = pitch;


        xyArray.add(new XYValue(x,y));
        x = x+.1f;

        createGraph();
    }

    private void createGraph()
    {

        xyArray = sortArray(xyArray);

        for (int i = 0; i < xyArray.size(); i ++)
        {
            try
            {
                float x = xyArray.get(i).getX();
                float y = xyArray.get(i).getY();
                xySeries.appendData(new DataPoint(x,y), true, 1000);
            }
            catch(IllegalArgumentException e)
            {

            }
        }



        fGraph.addSeries(xySeries);

    }

    //the array needs to be sorted with the x values ascending, otherwise it doesn't work.
    private ArrayList<XYValue> sortArray(ArrayList<XYValue> array){

        int factor = Integer.parseInt(String.valueOf(Math.round(pow(array.size(),2))));
        int m = array.size() - 1;
        int count = 0;


        while (true)
        {
            m--;
            if (m <= 0)
            {
                m = array.size() - 1;
            }//end if
            try
            {
                float tempY = array.get(m - 1).getY();
                float tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX())
                {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                }//end if
                else if (tempX == array.get(m).getX())
                {
                    count++;
                }//end else if
                else if (array.get(m).getX() > array.get(m - 1).getX())
                {
                    count++;
                }//end else if
                //break when factorial is done
                if (count == factor)
                {
                    break;
                }//end if
            }

            catch (ArrayIndexOutOfBoundsException e)
            {
                e.getMessage();
                break;
            }
        }
        return array;
    }

    public void toggleClick(View v)
    {
        if(tb.isChecked())
        {
            active = true;
            startRecording();
        }//end if

        else if(active)
        {
            active = false;
            stopRecording();
        }//end if
    }

    public void convertToNote(float pitch)
    {
        //Using standard A4 = 440Hz, C4 is 261.6Hz. Using C makes calculations easier.
        double C4 = 261.626;

        //octave grouping
        int octave = 4;

        //find the amount of half steps between A and the pitch I want to find
        double s = ((12)*Math.log(pitch/C4))/Math.log(2);
        String note[] = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        s = s%12;
        octave = (int)(octave - s);

        if(s < 0)
        {
            octave = octave*-1;
        }//end if

        //find note. Display closest note within half a semitone
        double x = s - Math.floor(s);

        //round to the nearest semitone
        int sRound = (int)Math.round(s);

        //add 12 if negative to get the right note value
        if(s < 0)
        {
            sRound+=12;
        }//end if
        if(sRound == 12)
        {
            sRound-=12;
        }//end if


        if(x >= .75 || x <= .25)
        {
            tvFreq.setText("" + pitch);
            tvNote.setText("" + note[sRound]);
            System.out.println("pitch is " + pitch + " Note is " + note[sRound] + octave);
        }//end if

        //otherwise just display the frequency
        else
        {
            tvFreq.setText("" + pitch);
        }//end else

    }
}
