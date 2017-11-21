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
import android.widget.Toast;
import android.widget.ToggleButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;

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
    TextView tv;
    ToggleButton tb;

    //graph variables
    LineGraphSeries<DataPoint> xySeries;
    private ArrayList<XYValue> xyArray;
    GraphView fGraph;
    public float pitch;
    public float x;
    public int count;
    public boolean active;
    Toast toast;
    Thread audioThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        }
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);

        tv = (TextView)findViewById(R.id.tv);
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

        //getPitch();

        double yinThreshold = 0.3;
        yin = new Yin(SAMPLERATE, BUFFER_SIZE, yinThreshold);
        startRecording();

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
                while (isRecording) {
                    recorder.read(sData, BUFFER_OVERLAY, diff);

                    for (int i = BUFFER_OVERLAY; i < diff; ++i) {
                        fData[i] = (float) sData[i];
                    }

                    float currentPitch = yin.getPitch(fData).getPitch();
                    if (currentPitch != -1 && currentPitch > 30) {
                        pitch = currentPitch;
                    }

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            updateNote(pitch);
                        }
                    });

                    for (int i = 0; i < BUFFER_OVERLAY; ++i) {
                        sData[i] = sData[i + diff];
                        fData[i] = (float) sData[i + diff];
                    }
                }
            }
        }.start();
    }

    private final NoteCalculator.NoteGuessResult guess = new NoteCalculator.NoteGuessResult();
    private synchronized void updateNote(final float pitch) {
        long currentTime = System.currentTimeMillis();
        if (lastUpdateTime < currentTime - UPDATE_DELAY)
        {

            tv.setText(String.format("%.1f (%.1f)", pitch, guess.realPitch));
            lastUpdateTime = currentTime;

            count++;

            //limit to once every 10 readings, otherwise it fills memory too quickly and crashes.
            if(count%4 == 0)
            {
                count = 0;
                init();
            }
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

        System.out.println(pitch);
        System.out.println(x);
        System.out.println(y);
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

        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(),2))));
        int m = array.size() - 1;
        int count = 0;


        while (true) {
            m--;
            if (m <= 0)
            {
                m = array.size() - 1;
            }
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
                }
                else if (tempX == array.get(m).getX())
                {
                    count++;
                }
                else if (array.get(m).getX() > array.get(m - 1).getX())
                {
                    count++;
                }
                //break when factorial is done
                if (count == factor)
                {
                    break;
                }
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
        toast = Toast.makeText(getApplicationContext(), "this works 2", Toast.LENGTH_SHORT);
        if(tb.isChecked())
        {
            toast = Toast.makeText(getApplicationContext(), "this works", Toast.LENGTH_SHORT);
            active = true;
            audioThread.start();
        }
        else if(active)
        {
            toast = Toast.makeText(getApplicationContext(), "this works", Toast.LENGTH_SHORT);
            active = false;
            audioThread.interrupt();
        }
    }

}
