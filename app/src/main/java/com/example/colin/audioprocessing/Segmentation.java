package com.example.colin.audioprocessing;

import java.util.ArrayList;

/**
 * Created by Colin on 27/03/2018.
 */

public class Segmentation
{

    int noteCount =0;
    public static ArrayList<String> noteList = new ArrayList<String>();
    Object[][] note_amp;
    int totalCount = 1;

    public Object[][] segmentation(String[] notes, Double[] amplitudes)
    {
        String[] n = notes;
        Double[] a = amplitudes;
        System.out.println(notes.length + " n  | " + amplitudes.length);
        note_amp = new Object[n.length][2];

        int firstRun = 0;
        for(int i = 0; i < n.length; i ++)
        {
            if(i != 0)
            {
                if(n[i] != null && n[i].equals(n[i-1]))
                {
                    noteCount++;
                }
                else
                {
                    note_amp[totalCount][0] = n[i-1];
                    note_amp[totalCount][1] = noteCount;

                    totalCount++;
                    noteCount = 1;
                }

            }
        }

        for(int i = 0; i < note_amp.length; i ++)
        {

           //System.out.println("Z " + note_amp[i][0] + " | " + note_amp[i][1]);
        }
        System.out.println("notamp len" + note_amp.length);
        noteCount ++;

        return note_amp;
    }


}
