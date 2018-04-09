package com.example.colin.audioprocessing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity
{
    private LiveProcessing lp = null;
    private WAVProcessing wp = null;

    //graph variables
    LineGraphSeries<DataPoint> xySeries;
    private ArrayList<XYValue> xyArray;
    GraphView fGraph;
    public float pitch;
    public float x;
    public int count;

    //General variables
    public static TextView tvFreq;
    public static TextView tvNote;
    public boolean active;
    public boolean begin;
    static Context context;
    public static Toolbar toolbar;
    public static ToggleButton tb;
    ArrayList<String> noteList = new ArrayList<String>();

    //database
    DatabaseManager db;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }//end if

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
        //open the database
        db= new DatabaseManager(this);

        try
        {
            db.open();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }



        //tvFreq = (TextView)findViewById(R.id.tvFreq);
        tvNote = (TextView)findViewById(R.id.tvNote);
        tb = (ToggleButton)findViewById(R.id.toggleButton);

        count = 0;
        xyArray = new ArrayList<>();
        fGraph = (GraphView) findViewById(R.id.fGraph);
        x = 0;
        active = false;
        begin = true;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent myIntent = new Intent(MainActivity.this, PianoRoll.class);
                //myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });



        Resources r = getResources();
        final int h = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 439,r.getDisplayMetrics()));
        fGraph.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this)
        {
            /*public void onSwipeTop()
            {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fGraph.getLayoutParams();
                params.height = h;
                fGraph.setLayoutParams(params);
                fGraph.getViewport().setMaxY(2500);
                tvFreq.setVisibility(View.VISIBLE);
                tvNote.setVisibility(View.VISIBLE);

            }*/
            public void onSwipeBottom()
            {

            }

        });


        displayGraphData();
        graphSettings();


        //set up toggle button for recording
        //Start recording when it's pressed, stop when released
        tb.setText("Record");
        tb.setTextOff("Record");
        tb.setTextOn("Stop");

        lp = new LiveProcessing();


        tb.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0)
            {
                if (tb.isChecked()==(true))
                {
                    //create an instance of the different processing methods
                    lp.startRecording();
                }
                else
                {
                    lp.stopRecording();
                }

            }});

        /*wp = new WAVProcessing();
        wp.readWav();*/

    }

    public void graphSettings()
    {
        //graph settings
        fGraph.getViewport().setScalable(true);
        fGraph.getViewport().setScrollable(true);
        fGraph.getViewport().setYAxisBoundsManual(true);
        fGraph.getViewport().setMaxY(2500);
        fGraph.getViewport().setMinY(0);
        fGraph.getViewport().setMinX(0);
    }

    public static void acceptNote(String note)
    {

    }
    public static void run()
    {}
    public void init()
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

        float x = 0;
        float y = 0;
        for (int i = 0; i < xyArray.size(); i ++)
        {
            try
            {
                x = xyArray.get(i).getX();
                y = xyArray.get(i).getY();
                xySeries.appendData(new DataPoint(x,y), true, 1000);
            }
            catch(IllegalArgumentException e)
            {

            }
        }

        fGraph.scrollTo((int)x,0);
        fGraph.addSeries(xySeries);
    }

    //the array needs to be sorted with the x values ascending, otherwise it will crash.
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

    //double tap to begin graphing, double tap again to stop
    public void displayGraphData()
    {
        if(begin == true)
        {
            begin = false;
            active = true;

        }//end if

        else if(active)
        {
            active = false;
            begin = true;
        }//end if

    }//end displayGraphData()



}