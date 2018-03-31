package com.example.colin.audioprocessing;

import android.support.v7.app.AppCompatActivity;
import java.text.DecimalFormat;

/**
 * Created by Colin on 27/03/2018.
 */

public class ProcessNote extends AppCompatActivity
{
    private static final int SAMPLERATE        = 44100;

    private static final int WINDOW_SIZE_PITCH    = 4096;
    private static final int WINDOW_OVERLAP_PITCH = WINDOW_SIZE_PITCH * 3/4;
    private Yin yin = null;
    float[] fData;
    String note;;

    public String processNote(float[] fData)
    {
        this.fData = fData;

        double yinThreshold = 0.3;
        yin = new Yin(SAMPLERATE, WINDOW_SIZE_PITCH, yinThreshold);
        float currentPitch = yin.getPitch(fData).getPitch();
        note = updateNote(currentPitch);
        return note;
    }

    private synchronized String updateNote(final float pitch)
    {
        String note = convertToNote(pitch);
        return note;
    }

    public String convertToNote(float pitch)
    {

        if(pitch == -1)
        {
            MainActivity.tvFreq.setText("-");
            return("-");
        }

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
            MainActivity.tvFreq.setText("" + f.format(pitch));
            MainActivity.tvNote.setText("" + note[sRound] + subscript[octave]);
            //System.out.println("pitch is " + pitch + " Note is " + note[sRound] + octave);
        }//end if

        //otherwise just display the frequency
        else
        {
            MainActivity.tvFreq.setText("" + f.format(pitch));
        }//end else

        if (pitch == -1)
        {
            return null;
        }
        return note[sRound];
    }
}
