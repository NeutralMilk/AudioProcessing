package com.example.colin.audioprocessing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity
{

    TextView tv;
    ToggleButton tb;
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
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        }

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
        //fGraph.setDrawBackground(true);
        fGraph.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        //set Scrollable and Scaleable
        fGraph.getViewport().setScalable(true);
        //fGraph.getViewport().setScalableY(true);
        fGraph.getViewport().setScrollable(true);
        //fGraph.getViewport().setScrollableY(true);

        fGraph.getViewport().setYAxisBoundsManual(true);
        fGraph.getViewport().setMaxY(2500);
        fGraph.getViewport().setMinY(0);

        //fGraph.getViewport().setXAxisBoundsManual(true);
        //fGraph.getViewport().setMaxX(1000);
        fGraph.getViewport().setMinX(0);

        getPitch();

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

        //xySeries.setShape(LineGraphSeries.Shape.POINT);
        //xySeries.setSize(5);

        //draw a custom shape
        /*xySeries.setCustomShape(new LineGraphSeries.CustomShape() {
            @Override
            public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                paint.setStrokeWidth(10);
                canvas.drawLine(x+5, y+5, x-5, y-5;, paint);
                //canvas.drawLine(x, y, x, y, paint);
            }
        });*/
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

    public void getPitch()
    {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100,2048,0);
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, 44100, 2048, handler);
        dispatcher.addAudioProcessor(pitchProcessor);

        audioThread = new Thread(dispatcher, "Audio Thread");
        //audioThread.start();


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
            active = false;
            audioThread.interrupt();
        }
    }
    PitchDetectionHandler handler = new PitchDetectionHandler() {
        @Override
        public void handlePitch(PitchDetectionResult res, AudioEvent e){
            final float pitchInHz = res.getPitch();
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    display(pitchInHz);
                }
            });
        }
    };


    public void display(float pitchInHz)
    {

        pitch = pitchInHz;
        tv.setText("" + pitchInHz);
        //System.out.println(pitch);

        count++;

        //limit to once every 10 readings, otherwise it fills memory too quickly and crashes.
        if(count%4 == 0)
        {
            count = 0;
            init();
        }
        //init();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}