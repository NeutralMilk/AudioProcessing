package com.example.colin.audioprocessing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.lang.Math.pow;
import static processing.core.PApplet.println;

public class MainActivity extends AppCompatActivity
{
    private LiveProcessing lp = null;
    private WAVProcessing wp = null;

    //graph variables
    static LineGraphSeries<DataPoint> xySeries;
    private static ArrayList<XYValue> xyArray;
    static GraphView fGraph;
    public static float x;
    public static int count;

    //General variables
    public static TextView tvFreq;
    public static TextView tvNote;
    public static ImageView piano;
    public static ImageView fileExplorer;
    public boolean active;
    public boolean begin;
    static Context context;
    public static Toolbar toolbar;
    public static ToggleButton tb;
    public static ArrayList<String> noteList = new ArrayList<String>();
    public static ArrayList<Float> noteLengthArraylist = new ArrayList<Float>();
    private static final int READ_REQUEST_CODE = 42;


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
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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

        piano = (ImageView) findViewById(R.id.piano);
        piano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Float[] noteLength = new Float[noteLengthArraylist.size()];
                for(int i = 0; i < noteLengthArraylist.size(); i ++)
                {
                    noteLength[i] = noteLengthArraylist.get(i);
                }
                for(int i = 0; i < noteLength.length;i ++)
                {
                    System.out.println(noteLength[i]);
                }
                Intent myIntent = new Intent(MainActivity.this, PianoRoll.class);
                myIntent.putExtra("notes", noteList); //Optional parameters
                myIntent.putExtra("noteTimes", noteLength);
                MainActivity.this.startActivity(myIntent);
            }
        });

        fileExplorer = (ImageView) findViewById(R.id.file);
        fileExplorer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String wavPath = MainActivity.context.getFilesDir() + "/" + "scale.wav";
                final File wF = new File(wavPath);
                wp = new WAVProcessing();
                wp.readWav(wF);
                System.out.println("List of notes is" + noteList);
                System.out.println("Length of notes is " + noteLengthArraylist);

                /*// ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // Filter to show only wav files
                intent.setType("audio/wav");
                startActivityForResult(intent, READ_REQUEST_CODE);*/
            }
        });


        Resources r = getResources();

        displayGraphData();
        graphSettings();


        //set up toggle button for recording
        //Start recording when it's pressed, stop when released
        tb.setText("Record");
        tb.setTextOff("Record");
        tb.setTextOn("Stop");

        lp = new LiveProcessing();


        tb.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                if (tb.isChecked()==(true))
                {
                    //create an instance of the different processing methods
                    noteList.clear();
                    lp.startRecording();
                }
                else
                {
                    lp.stopRecording();
                }

            }
        });

        /*wp = new WAVProcessing();
        wp.readWav();*/

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        Uri uri = null;

        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == READ_REQUEST_CODE)
            {

                if (resultData != null)
                {
                    uri = resultData.getData();
                    System.out.println(uri);
                    //uri is null
                    //will always fail the try
                    /*try
                    {*/
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        //System.out.println("uri is " + uri.toString());
                        String path = uri.getPath();
                        try
                        {
                            File wavFile = new File(new URI(path));

                        }
                        catch(URISyntaxException e)
                        {

                        }
                    /*}
                    catch(Exception e)
                    {
                        System.out.println("didn't work ");
                    }
*/
                }
            }
        }
    }

    /*wp = new WAVProcessing();
                            wp.readWav(wavFile);*/

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


    static public void init(float pitch)
    {
        xySeries = new LineGraphSeries<>();
        float y = pitch;

        xyArray.add(new XYValue(x,y));
        x = x+.1f;

        createGraph();
    }

    private static void createGraph()
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
    private static ArrayList<XYValue> sortArray(ArrayList<XYValue> array){

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