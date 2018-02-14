package com.example.colin.audioprocessing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity
{

    //pitch settings
    private static final int SAMPLERATE        = 44100;
    private static final int NUM_CHANNELS      = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int WINDOW_SIZE    = 4096;
    private static final int WINDOW_OVERLAP = WINDOW_SIZE * 3/4;
    private static final int FRAMERATE    = 60;
    private static final int UPDATE_DELAY = 1000/FRAMERATE;
    private AudioRecord recorder    = null;
    private boolean isRecording = false;
    private Yin yin = null;
    private long lastUpdateTime = 0;
    String previousNote;
    String currentNote;
    long currentTime;
    long previousTime;
    String[] adjacentNotes = {"0", "0"};
    long[] adjacentTimes = {0,0};
    long counter = 0;


    //graph variables
    LineGraphSeries<DataPoint> xySeries;
    private ArrayList<XYValue> xyArray;
    GraphView fGraph;
    public float pitch;
    public float x;
    public int count;
    private float maxF;


    //General variables
    TextView tvFreq;
    TextView tvNote;
    public boolean active;
    public boolean begin;
    public long clickTime;
    private GestureDetector gd;
    View view;
    public LinearLayout l;
    Switch s;
    long time;
    DataOutputStream output;
    File root;
    File gpxfile;
    FileWriter writer;
    private static final String FILENAME = "myFile.txt";
    private static final String TAG = MainActivity.class.getName();
    File path;
    File file;
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
        try
        {
            db.open();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        //checkDataBase();

        try
        {
            out = new OutputStreamWriter(openFileOutput("save.txt", MODE_APPEND));
        }
        catch(IOException e)
        {

        }
        /*root = new File(Environment.getExternalStorageDirectory(), "Notes");
        if (!root.exists()) {
            root.mkdirs();
        }

        gpxfile = new File(root, "samples.txt");
        try
        {
            writer = new FileWriter(gpxfile);
        }
        catch(IOException e)
        {

        }*/

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
        yin = new Yin(SAMPLERATE, WINDOW_SIZE, yinThreshold);

        startRecording();
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        String DB_FULL_PATH = "/data/data/com.example.colin.audioprocessing/databases";
        try {
            checkDB = SQLiteDatabase.openDatabase(DB_FULL_PATH, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            System.out.println("DB doesn't exist");
        }
        return checkDB != null;
    }
    String printNote;
    //The startRecording method is adapted from https://github.com/solarus/CTuner/blob/master/src/org/tunna/ctuner/MainActivity.java
    private void startRecording() {
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLERATE, NUM_CHANNELS, RECORDER_ENCODING);
        final int bufferSize = Math.max(minBufferSize, WINDOW_SIZE);

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLERATE,
                NUM_CHANNELS,
                RECORDER_ENCODING,
                bufferSize);
        recorder.startRecording();
        isRecording = true;

        new Thread() {
            public void run()
            {
                final short[] sData = new short[WINDOW_SIZE];
                final float[] fData = new float[WINDOW_SIZE];

                final int diff = bufferSize - WINDOW_OVERLAP;

                // This loop will be correct after 3 rounds because of
                // the WINDOW_OVERLAP offset
                while (isRecording)
                {
                    int readSize = recorder.read(sData, WINDOW_OVERLAP, diff);

                    for (int i = WINDOW_OVERLAP; i < diff; ++i)
                    {
                        fData[i] = (float) sData[i];
                        Log.v(String.valueOf(fData[i]), "fdata");
                        //System.out.println("fdata is" + fData[i]);
                    }//end for



                    float currentPitch = yin.getPitch(fData).getPitch();
                    //Set the minimum pitch value to be 60Hz. I do this to allow for alternate tunings such as drop D
                    //This is when the low E string is tuned down 2 semitones to a D
                    //Also, the default output when no pitch is detected is - 1. This causes the graph to look funny
                    //when there's no input because it will shoot down to -1, so it's best to ignore these values.
                    if (currentPitch > -1 && currentPitch < 4000) {
                        pitch = currentPitch;
                    }//end if

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            printNote = updateNote(pitch);
                        }
                    });

                    for (int i = 0; i < WINDOW_OVERLAP; ++i) {
                        sData[i] = sData[i + diff];
                        fData[i] = (float) sData[i + diff];
                    }//end for

                    double sum = 0;
                    for (int i = 0; i < readSize; i++)
                    {
                        sum += sData [i] * sData [i];
                    }//end for

                    if (readSize > 0)
                    {
                        final double amplitude = sum / readSize;

                        try
                        {
                            /*writer.append(Integer.toString((int) Math.sqrt(amplitude)));
                            writer.flush();
                            writer.close();*/

                            out = new OutputStreamWriter(openFileOutput("save.txt", MODE_APPEND));
                            out.write(Integer.toString((int) Math.sqrt(amplitude)) + " " + printNote);
                            out.write("\r\n");

                            //close file

                            out.close();
                        }

                        catch(IOException e)
                        {
                            System.out.println("this aint woikin");
                        }

                        System.out.println((int) Math.sqrt(amplitude));
                    }//end if
                }
            }
        }.start();
    }
    FileWriter fw;

    public String getPreviousPitch()
    {

        return previousNote;
    }

    private synchronized String updateNote(final float pitch) {

        String note = convertToNote(pitch);
        //System.out.println("note is " + note);
        currentTime = System.currentTimeMillis();
        currentNote = note;
        if (lastUpdateTime < currentTime - UPDATE_DELAY)
        {


            lastUpdateTime = currentTime;

            count++;

            //limit to once every 10 readings, otherwise it fills memory too quickly and crashes.
            //time = System.currentTimeMillis();
            //System.out.println("current time is " + currentTime);



            //set the very first previous note to be the current note.
            //there is no 'previous' note at this point but it can't be left empty.
            if (adjacentNotes[1] == "0")
            {
                previousNote = currentNote;
                adjacentNotes[0] = previousNote;
                adjacentNotes[1] = previousNote;

                previousTime = currentTime;
                adjacentTimes[0] = previousTime;
                adjacentTimes[1] = previousTime;
            }

            //check if the two adjacent notes are the same.
            //if they are the same, add the time difference to a counter.
            if(currentNote == previousNote)
            {
                long diff = currentTime - previousTime;

                counter += diff;
            }//end if

            else
            {
                //System.out.println("the note played was " + previousNote + " and it lasted for " + counter + "ms");
                //db.insertNotes("1",previousNote,Long.toString(counter));
                previousTime = currentTime;
                previousNote = currentNote;
                counter = 0;
            }
            if(active)
            {
                if(count%4 == 0)
                {
                    count = 0;


                }//end if
            }

        }
        return note;
    }

    public void graphSettings()
    {
        //graph settings
        //fGraph.setBackgroundColor(getResources().getColor(android.R.color.white));
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

    public String convertToNote(float pitch)
    {

        //list of notes
        String note[] = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        //Using the standard A4 = 440Hz, C4 is 261.6Hz. Using C makes calculations simpler since it's my first element.
        double C4 = 261.626;

        //octave grouping, this will tell me if a note is an E4, F6, C2 etc.
        int octave = 4;
        double o = 0;
        //Java doesn't have built in subscript so I'll use an array of the unicode values
        String subscript[] = {"\u2080", "\u2081", "\u2082", "\u2083", "\u2084", "\u2085", "\u2086", "\u2087", "\u2088", "\u2089"};

        //find the amount of semitones between A and the pitch I want to find
        double s = ((12)*Math.log(pitch/C4))/Math.log(2);

        //calculate octave range
        o = s/12;
        s = s%12;
        octave = octave + (int)o;
        if(octave < 4)
        {
            octave-=1;
        }
        //System.out.println("o is" + (int)o);


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


        DecimalFormat f = new DecimalFormat("##.000");
        //display closest note within a 1/4 semitone
        double x = s - Math.floor(s);
        if(x >= .75 || x <= .25)
        {
            tvFreq.setText("" + f.format(pitch));
            tvNote.setText("" + note[sRound] + subscript[octave]);
            //System.out.println("pitch is " + pitch + " Note is " + note[sRound] + octave);
        }//end if

        //otherwise just display the frequency
        else
        {
            tvFreq.setText("" + f.format(pitch));
        }//end else

        return note[sRound];
    }
}
