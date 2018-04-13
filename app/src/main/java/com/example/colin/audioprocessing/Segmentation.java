package com.example.colin.audioprocessing;

import java.util.ArrayList;

/**
 * Created by Colin on 27/03/2018.
 */

public class Segmentation
{

    int noteCount =0;
    Object[][] note_amp;
    int totalCount = 1;
    int lowerThreshold = 300;
    int upperThreshold = 500;

    public Object[][] segmentation(String[] notes, Double[] amplitudes)
    {
        String[] n = notes;
        Double[] a = amplitudes;
        note_amp = new Object[n.length][2];

        //this will weed out anywhere that an amplitude is detected that should not count
        for(int i = 0; i < n.length; i ++)
        {
            if(amplitudes[i] != null && amplitudes[i] < lowerThreshold)
            {
                n[i] = "-";
            }
            if(i > 1 && i < n.length-2)
            {
                //if there is a 'break' in the stream of notes while a note is being detected
                if(n[i-1].equals(notes[i+1]) && n[i].equals("-"))
                {
                    //if the notes surrounding that break are different
                    if(!n[i-2].equals(notes[i-1]) || !n[i+2].equals(n[i-1]))
                    {
                        //if that amplitude is big enough to be considered a note
                        if(amplitudes[i] > upperThreshold)
                        {
                            //fill in that blank with the correct note.
                            n[i] = n[i+1];
                        }
                    }
                }
            }
        }

        for(int i = 0; i < n.length-1; i ++)
        {
            if(i > 1 && i < n.length-1)
            {
                if(!n[i].equals("-"))
                {
                    if(!n[i+1].equals("-"))
                    {
                        if(!n[i].equals(n[i+1]))
                        {
                            if(n[i+2].equals("-"))
                            {
                                n[i + 1] = n[i];
                            }
                        }
                    }
                }
            }

            if(i > 1 && i < n.length-1)
            {
                if(!n[i].equals("-"))
                {
                    if(!n[i-1].equals("-"))
                    {
                        if(!n[i].equals(n[i-1]))
                        {
                            if(n[i-2].equals("-"))
                            {
                                n[i - 1] = n[i];
                            }
                        }
                    }
                }
            }

            //if there is a single note, it's too short
            if(i > 1 && i < n.length-1)
            {
                if(!n[i].equals("-"))
                {
                    if(n[i-1].equals("-"))
                    {
                        if(n[i+1].equals("-"))
                        {

                            n[i] = "-";

                        }
                    }
                }
            }


            if(!n[i].equals("-"))
            {
                char fl = n[i].charAt(0);
                char sl;
                String note = new StringBuilder().append(fl).toString();
                if(!Character.isDigit(n[i].charAt(1)))
                {
                    sl = n[i].charAt(1);
                    note = new StringBuilder().append(fl).append(sl).toString();
                }

                if(n[i+1].contains(note))
                {
                    n[i+1] = n[i];
                }
            }

        }

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

        noteCount ++;
        return note_amp;
    }


}
