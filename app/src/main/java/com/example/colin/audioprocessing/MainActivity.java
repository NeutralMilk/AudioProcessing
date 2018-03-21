package com.example.colin.audioprocessing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.media.AudioFormat;
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
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity
{

    //pitch settings
    private static final int SAMPLERATE        = 44100;
    private static final int NUM_CHANNELS      = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int WINDOW_SIZE_PITCH    = 4096;
    private static final int WINDOW_SIZE_AMP    = 4096;
    private static final int WINDOW_OVERLAP_PITCH = WINDOW_SIZE_PITCH * 3/4;
    private static final int WINDOW_OVERLAP_AMP = WINDOW_SIZE_AMP * 3/4;
    private static final int FRAMERATE    = 60;
    private static final int UPDATE_DELAY = 1000/FRAMERATE;
    private AudioRecord recorder    = null;
    private AudioTrack audioTrack = null;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private Yin yin = null;
    private long lastUpdateTime = 0;
    String previousNote;
    String currentNote;
    long currentTime;
    long previousTime;
    String[] adjacentNotes = {"0", "0"};
    long[] adjacentTimes = {0,0};


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
        try
        {
            db.open();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new OutputStreamWriter(openFileOutput("save.txt", MODE_APPEND));
        }
        catch(IOException e)
        {

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
        yin = new Yin(SAMPLERATE, WINDOW_SIZE_PITCH, yinThreshold);

        startRecording();
    }

    String printNote;
    //The startRecording method is adapted from https://github.com/solarus/CTuner/blob/master/src/org/tunna/ctuner/MainActivity.java
    private void startRecording()
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
                final short[] sDataPitch = new short[WINDOW_SIZE_PITCH];
                final float[] fData = new float[WINDOW_SIZE_PITCH];
                final short[] sDataAmp = new short[WINDOW_SIZE_AMP];

                final int diffPitch = WINDOW_SIZE_PITCH - WINDOW_OVERLAP_PITCH;
                final int diffAmp = WINDOW_SIZE_AMP - WINDOW_OVERLAP_AMP;

                // This loop will be correct after 3 rounds because of
                // the WINDOW_OVERLAP offset
                while (isRecording)
                {
                    recorder.read(sDataPitch, WINDOW_OVERLAP_PITCH, diffPitch);
                    int readSize = recorder.read(sDataAmp, WINDOW_OVERLAP_AMP, diffAmp);

                    //System.out.println("readsize is " + readSize);
                    for (int i = WINDOW_OVERLAP_PITCH; i < diffPitch; ++i)
                    {
                        fData[i] = (float) sDataPitch[i];
                        Log.v(String.valueOf(fData[i]), "fdata");
                        //System.out.println("fdata is" + fData[i]);
                    }//end for

                    float currentPitch = yin.getPitch(fData).getPitch();

                    pitch = currentPitch;

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            printNote = updateNote(pitch);
                        }
                    });

                    for (int i = 0; i < WINDOW_OVERLAP_PITCH; ++i)
                    {
                        sDataPitch[i] = sDataPitch[i + diffPitch];
                        fData[i] = (float) sDataPitch[i + diffPitch];
                    }//end for

                    double amplitude[] = new double[4];

                    for(int j = 0 ; j < 4; j++)
                    {
                        double sum = 0;
                        for(int i = 0; i < readSize; i++)
                        {
                            sum += sDataPitch [i*(j+1)] * sDataPitch [i*(j+1)];
                        }//end for

                        boolean valid = false;
                        if (readSize >= 0)
                        {
                            amplitude[j] = sum / readSize;
                            try
                            {
                                out = new OutputStreamWriter(openFileOutput("save.txt", MODE_APPEND));
                                out.write(Integer.toString((int) Math.sqrt(amplitude[j])) + " | " + pitch + "Hz | " + printNote);
                                out.write("\r\n");
                                out.close();
                            }
                            catch(IOException e)
                            {
                                System.out.println("not working");
                            }

                            if(j == 0)
                            {
                                System.out.println(Integer.toString((int) Math.sqrt(amplitude[j])) + " | " + pitch + "Hz | " + printNote);
                            }
                            else
                            {
                                System.out.println(Integer.toString((int) Math.sqrt(amplitude[j])));
                            }
                            amplitude[j] = Math.sqrt(amplitude[j]);
                            valid = segmentation(amplitude[], pitch);
                        }//end if
                    }

                    count++;

                    if(active)
                    {
                        if(count%4 == 0)
                        {
                            count = 0;
                            init();
                        }//end if
                    }
                }
            }
        }.start();
    }

    private boolean segmentation(double a[], float p)
    {
        boolean valid = true;
        double amplitude[] = a;
        float pitch = p;
        return valid;
    }
    private synchronized String updateNote(final float pitch)
    {
        String note = convertToNote(pitch);
        //System.out.println("note is " + note);
        currentNote = note;

        //set the very first previous note to be the current note.
        //there is no 'previous' note at this point but it can't be left empty.
        if (adjacentNotes[1] == "0")
        {
            currentTime = System.currentTimeMillis();
            previousNote = currentNote;
            adjacentNotes[0] = previousNote;
            adjacentNotes[1] = previousNote;

            previousTime = currentTime;
            adjacentTimes[0] = previousTime;
            adjacentTimes[1] = previousTime;
        }
        //if the two notes are the same, previous time will be set to current time
        //when they change, we can subtract the previous time from current time
        if(previousNote != currentNote)
        {
           previousTime = currentTime;
           currentTime = System.currentTimeMillis();
           long diff = currentTime - previousTime;
           System.out.println("the note played was " + previousNote + " and it lasted for " + diff + "ms" );
        }
        previousNote = currentNote;

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
        String note[] = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        /*if(currentNote == note[0])
        {
            xySeries.setColor(Color.rgb(255,0,0));
        }
        if(currentNote == note[1])
        {
            xySeries.setColor(Color.rgb(255,0,179));
        }
        if(currentNote == note[2])
        {
            xySeries.setColor(Color.rgb(222,0,255));
        }
        if(currentNote == note[3])
        {
            xySeries.setColor(Color.rgb(111,0,255));
        }
        if(currentNote == note[4])
        {
            xySeries.setColor(Color.rgb(0,51,255));
        }
        if(currentNote == note[5])
        {
            xySeries.setColor(Color.rgb(0,205,255));
        }
        if(currentNote == note[6])
        {
            xySeries.setColor(Color.rgb(0,255,188));
        }
        if(currentNote == note[7])
        {
            xySeries.setColor(Color.rgb(0,255,51));
        }
        if(currentNote == note[8])
        {
            xySeries.setColor(Color.rgb(85,255,0));
        }
        if(currentNote == note[9])
        {
            xySeries.setColor(Color.rgb(222,255,0));
        }
        if(currentNote == note[10])
        {
            xySeries.setColor(Color.rgb(255,205,0));
        }
        if(currentNote == note[11])
        {
            xySeries.setColor(Color.rgb(255,94,0));
        }*/
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

        if (pitch == -1)
        {
            return null;
        }
        return note[sRound];
    }
}
