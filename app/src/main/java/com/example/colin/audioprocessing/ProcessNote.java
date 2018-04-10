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

    String[] notes =
            {
                    "C8","B7","A#7","A7","G#7","G7","F#7","F7",
                    "E7","D#7","D7","C#7","C7","B6","A#6","A6",
                    "G#6","G6","F#6","F6","E6","D#6","D6","C#6",
                    "C6 ","B5","A#5","A5","G#5","G5","F#5","F5",
                    "E5","D#5","D5","C#5","C5","B4","A#4","A4",
                    "G#4","G4","F#4","F4","E4","D#4","D4","C#4",
                    "C4","B3","A#3","A3","G#3","G3","F#3","F3",
                    "E3","D#3","D3","C#3","C3","B2","A#2","A2",
                    "G#2","G2","F#2","F2","E2","D#2","D2","C#2",
                    "C2","B1","A#1","A1","G#1","G1","F#1","F1",
                    "E1","D#1","D1","C#1","C1","B0","A#0","A0"
            };
    public String processNote(float[] fData)
    {
        this.fData = fData;

        double yinThreshold = 0.3;
        yin = new Yin(SAMPLERATE, WINDOW_SIZE_PITCH, yinThreshold);
        float currentPitch = yin.getPitch(fData).getPitch();
        //MainActivity.init(currentPitch);
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
            //MainActivity.tvFreq.setText("-");

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
            //MainActivity.tvFreq.setText("" + f.format(pitch));
            MainActivity.tvNote.setText("" + note[sRound] + subscript[octave]);

            //System.out.println("pitch is " + pitch + " Note is " + note[sRound] + octave);
        }//end if

        //otherwise just display the frequency
        else
        {
            MainActivity.tvNote.setText("-");
        }//end else

        if (pitch == -1)
        {
            return null;
        }

        //boolean noteExists = false;
        for(int j = 0; j<notes.length;j++)
        {
            if((note[sRound]+octave).equals(notes[j]))
            {
                return note[sRound]+octave;

            }
        }
        return "-";
    }
}
