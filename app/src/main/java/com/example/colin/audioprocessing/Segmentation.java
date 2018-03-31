package com.example.colin.audioprocessing;

/**
 * Created by Colin on 27/03/2018.
 */

public class Segmentation
{

    String previousNote;
    String currentNote;
    long currentTime;
    long previousTime;
    Short[] audioShorts;
    double[] amplitude = new double[4];
    String note;
    boolean valid = false;
    int count = 1;


    /*
    * A sample rate of 44.1kHz and a window size of 4096 will give 10.766 readings per second
    * Counting the number of readings taken can then be used to determine the time taken
    * 1 reading would take 1/10.766 or 0.09288 seconds
    * For amplitude readings there are 4 times as many readings because the window size is 1024
    * one amplitude reading will take 0.02322 seconds
    * this is more reliable than using a timer as it very easily produces incorrect readings, form my testing.
    */

    float noteTime = 0.09287981859f;
    float ampTime = 0.02321995464f;

    public boolean segmentation(String note, double[] amplitude)
    {
        this.note = note;
        this.amplitude = amplitude;
        boolean validNote = validNote(note);
        count ++;
        validAmplitude(amplitude);
        /*if(validNote)
        {
            MainActivity.acceptNote();
        }*/
        return valid;
    }

    private boolean validNote(String n)
    {
        boolean validNote = false;

        //if there is no note, it's immediately ignored
        if(n == "-")
        {
             return validNote;
        }
        //if a note lasts two readings or less, it can be ignored.
        //however we need to have 3 readings to determine if only two were the same
        //to do this I'll make a note when there is two of the same readings
        //then, if the third is the same it will be valid, but if it's different then it is invalid.
        else if(count * noteTime <= noteTime*2)
        {
            if(previousNote == currentNote)
            {

                return true;
            }
            previousNote = currentNote;
        }
        return validNote;
    }
    private boolean validAmplitude(double[] a)
    {
        boolean validAmplitude = false;
        return validAmplitude;
    }
}
