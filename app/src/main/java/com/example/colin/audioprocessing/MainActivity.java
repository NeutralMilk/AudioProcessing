package com.example.colin.audioprocessing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.ToggleButton;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private PostProcessing wp = null;

    //graph variables
    static LineGraphSeries<DataPoint> xySeries;
    public static float x;
    public static int count;

    //General variables
    public static ImageView piano;
    public static ImageView fileExplorer;
    public static ImageView fileExplorer2;
    public static ImageView fileExplorer3;

    public boolean active;
    public boolean begin;
    static Context context;
    public static ImageButton b;
    public static ArrayList<String> noteListPost = new ArrayList<String>();
    public static ArrayList<Float> noteLengthArraylist = new ArrayList<Float>();
    private static final int READ_REQUEST_CODE = 42;
    public static Object[][] note_time;



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


        count = 0;
        x = 0;
        active = false;
        begin = true;

        piano = (ImageView) findViewById(R.id.piano);
        piano.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Float[] noteLength = new Float[noteLengthArraylist.size()];
                for(int i = 0; i < noteLengthArraylist.size(); i ++)
                {
                    noteLength[i] = noteLengthArraylist.get(i);
                }

                Intent myIntent = new Intent(MainActivity.this, LivePianoRoll.class);
                myIntent.putExtra("notes", noteListPost); //Optional parameters
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
                noteListPost.clear();
                noteLengthArraylist.clear();
                InputStream wf = getResources().openRawResource(R.raw.scale4);
                //final File wF = new File(wavPath);
                wp = new PostProcessing();
                note_time = wp.readWav(wf);


                Intent myIntent = new Intent(MainActivity.this, PostPianoRoll.class);
                MainActivity.this.startActivity(myIntent);
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

        fileExplorer2 = (ImageView) findViewById(R.id.file2);
        fileExplorer2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                noteListPost.clear();
                noteLengthArraylist.clear();
                InputStream wf = getResources().openRawResource(R.raw.waltz);
                //final File wF = new File(wavPath);
                wp = new PostProcessing();
                note_time = wp.readWav(wf);


                Intent myIntent = new Intent(MainActivity.this, PostPianoRoll.class);
                MainActivity.this.startActivity(myIntent);
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

        fileExplorer3 = (ImageView) findViewById(R.id.file3);
        fileExplorer3.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                noteListPost.clear();
                noteLengthArraylist.clear();
                InputStream wf = getResources().openRawResource(R.raw.beach);
                //final File wF = new File(wavPath);
                wp = new PostProcessing();
                note_time = wp.readWav(wf);


                Intent myIntent = new Intent(MainActivity.this, PostPianoRoll.class);
                MainActivity.this.startActivity(myIntent);
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
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    String path = uri.getPath();
                    try
                    {
                        File wavFile = new File(new URI(path));

                    }
                    catch(URISyntaxException e)
                    {

                    }

                }
            }
        }
    }
}