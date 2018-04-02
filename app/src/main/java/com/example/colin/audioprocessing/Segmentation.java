package com.example.colin.audioprocessing;

/**
 * Created by Colin on 27/03/2018.
 */

public class Segmentation
{
    String previousNote;
    String currentNote;
    double amplitude;
    int count;

    /*
    * validity will be determined by using an int between 0 and 2
    * 0 for invalid
    * 1 for valid
    * 2 for 'to be determined'
    */


    /*
    * A sample rate of 44.1kHz and a window size of 1024 will give 43.0664 readings per second
    * Counting the number of readings taken can then be used to determine the time taken
    * 1 reading would take 1/43.0664 or 0.02322 seconds
    * this is more reliable than using a timer as it very easily produces incorrect readings, from my testing.
    */

    float time = 0.02321995464f;

    public float segmentation(String currentNote, double amplitude, String previousNote, int count)
    {
        this.previousNote = previousNote;
        this.currentNote = currentNote;
        this.amplitude = amplitude;
        this.count = count;
        float validNote = validNote();
        validAmplitude(amplitude);
        /*if(validNote)
        {
            MainActivity.acceptNote();
        }*/

        return validNote;
    }

    private float validNote()
    {

        float returnVal = 0;
        //if there is no note, it's immediately ignored
        if(currentNote != null && currentNote.equals("-") == true)
        {
             returnVal = 0;
        }
        //if a note lasts two readings or less, it can be ignored.
        //however we need to have 3 readings to determine if only two were the same
        //to do this I'll make a note when there is two of the same readings
        //then, if the third is the same it will be valid, but if it's different then it is invalid.
        //if the previous note is not the same as the current note, then this is a new note and we can return the count*time
        if(previousNote != null && previousNote.equals(currentNote) == false)
        {
            returnVal = count*time;
        }
        else
        {
            returnVal = 0;
        }

        return returnVal;
    }
    private int validAmplitude(double a)
    {
        return 2;
    }
}
