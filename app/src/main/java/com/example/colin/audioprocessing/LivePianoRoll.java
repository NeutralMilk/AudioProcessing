package com.example.colin.audioprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;

import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

/**
 * Created by Colin on 08/04/2018.
 */

public class LivePianoRoll extends AppCompatActivity
{
    private PApplet sketch;
    public static ArrayList<String> notes = new ArrayList<String>();
    public static float[] noteLength;
    private LiveProcessing lp = null;
    public static String note;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.piano_roll);

        Intent intent = getIntent();

        lp = new LiveProcessing();
        lp.startRecording();
        notes = intent.getStringArrayListExtra("notes");
        noteLength = intent.getFloatArrayExtra("noteLength");
        System.out.println(notes);
        for(int i = 0; i < MainActivity.noteLengthArraylist.size();i ++)
        {
            System.out.println(MainActivity.noteLengthArraylist.get(i));
        }
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        sketch = new LiveSketch();
        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (sketch != null) {
            sketch.onRequestPermissionsResult(
                    requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (sketch != null) {
            sketch.onNewIntent(intent);
        }
    }
}
