package com.example.colin.audioprocessing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity
{

    //audio settings
    private static final int SAMPLERATE        = 44100;
    private static final int NUM_CHANNELS      = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int WINDOW_SIZE_PITCH    = 4096;
    private static final int WINDOW_SIZE_AMP    = 1024;
    private static final int WINDOW_OVERLAP_PITCH = WINDOW_SIZE_PITCH * 3/4;
    private static final int WINDOW_OVERLAP_AMP = WINDOW_SIZE_AMP * 3/4;
    private static final int FRAMERATE    = 60;
    byte[] bytes;
    private static final int UPDATE_DELAY = 1000/FRAMERATE;
    private AudioRecord recorder    = null;
    private AudioTrack audioTrack = null;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private Yin yin = null;
    private LiveProcessing lp = null;
    private long lastUpdateTime = 0;
    String previousNote;
    String currentNote;
    long currentTime;
    long previousTime;
    String[] adjacentNotes = {"0", "0"};
    long[] adjacentTimes = {0,0};
    boolean successfulRead = false;


    //graph variables
    LineGraphSeries<DataPoint> xySeries;
    private ArrayList<XYValue> xyArray;
    GraphView fGraph;
    public float pitch;
    public float x;
    public int count;
    private float maxF;


    //General variables
    public static TextView tvFreq;
    public static TextView tvNote;
    public boolean active;
    public boolean begin;
    public long clickTime;
    Switch s;

    OutputStreamWriter out;

    //database
    DatabaseManager db;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }//end if

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //open the database
        db= new DatabaseManager(this);

        File file = new File("/data/data/com.example.colin.audioprocessing/files/scale.wav/");
        int size = (int) file.length();
        bytes = new byte[size];
        try
        {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try
        {
            db.open();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        tvFreq = (TextView)findViewById(R.id.tvFreq);
        tvNote = (TextView)findViewById(R.id.tvNote);
        s = (Switch)findViewById(R.id.record);

        count = 0;
        xyArray = new ArrayList<>();
        fGraph = (GraphView) findViewById(R.id.fGraph);
        x = 0;
        active = false;
        begin = true;

        Resources r = getResources();
        final int h = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 439,r.getDisplayMetrics()));
        fGraph.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this)
        {
            public void onSwipeTop()
            {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fGraph.getLayoutParams();
                params.height = h;
                fGraph.setLayoutParams(params);
                fGraph.getViewport().setMaxY(2500);
                tvFreq.setVisibility(View.VISIBLE);
                tvNote.setVisibility(View.VISIBLE);

            }
            public void onSwipeBottom()
            {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fGraph.getLayoutParams();
                params.height = MATCH_PARENT;
                fGraph.setLayoutParams(params);
                fGraph.getViewport().setMaxY(3000);
                tvFreq.setVisibility(View.INVISIBLE);
                tvNote.setVisibility(View.INVISIBLE);
            }

        });

        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                displayGraphData();
            }
        });

        graphSettings();

        double yinThreshold = 0.3;
        System.out.println("is anything working");
        yin = new Yin(SAMPLERATE, WINDOW_SIZE_PITCH, yinThreshold);
        lp = new LiveProcessing();
        lp.startRecording();
        //startRecording();
        //readWav();
    }

    String printNote;



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