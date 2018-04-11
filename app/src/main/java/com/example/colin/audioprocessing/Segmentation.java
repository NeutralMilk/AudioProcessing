package com.example.colin.audioprocessing;

/**
 * Created by Colin on 27/03/2018.
 */

public class Segmentation
{
    String[] notes;
    Double[] amplitudes;
    int noteCount =0;
    int
    float time = 0.02321995464f;

    public Object[][] segmentation(String[] notes, Double[] amplitudes)
    {
        this.notes = notes;
        this.amplitudes = amplitudes;
        int firstRun = 0;
        for(int i = 0; i < notes.length; i ++)
        {
            if(i == 0)
            {
                if(notes[i] == notes[i-firstRun])
                {
                        noteCount++;
                }
            }
            while()
        }
        noteCount ++;
        Object[][] note_amp= new Object[2][2];
        return note_amp;
    }


}
