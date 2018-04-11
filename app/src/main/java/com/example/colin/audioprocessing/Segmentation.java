package com.example.colin.audioprocessing;

/**
 * Created by Colin on 27/03/2018.
 */

public class Segmentation
{
    String previousNote;
    String currentNote;
    double currentAmp;
    double previousAmp;
    int noteCount =0;
    int ampCount = 0;
    float currentAmplitude;
    float previousAmplitude;

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

    public float segmentation(String currentNote, double currentAmp)
    {
        this.previousNote = previousNote;
        this.currentNote = currentNote;
        this.currentAmp = currentAmp;
        this.previousAmp = previousAmp;
        float validNote = validNote();
        float validAmp = validAmplitude(currentAmp);


        noteCount ++;
        previousNote = currentNote;
        previousAmp = currentAmp;

        if (validNote > 0 )
        {
            //System.out.println("this was a valid note");
            if(previousNote != "-")
            {
                MainActivity.noteList.add(previousNote);
                MainActivity.noteLengthArraylist.add(validNote);
            }
        }
        else
        {

            //System.out.println(currentNote + "|" + validAmp + "|" + currentAmp);
        }
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
        //if the previous note is not the same as the current note, then this is a new note and we can return the count*time
        if(previousNote != null && previousNote.equals(currentNote) == false)
        {
            returnVal = noteCount*time;

            //ignore notes that fall below the time threshold
            if(returnVal < time*2)
            {
                returnVal = 0;

            }
            noteCount = 0;
        }
        else
        {
            returnVal = 0;
        }

        return returnVal;
    }
    private float validAmplitude(double ca)
    {
        int lowerThreshold = 200;
        int higherThreshold = 300;
        float returnVal = 0;

        /*
        * There are three problems with amplitude segmentation
        * 1. Extraneous background noise or from a crackling microphone
        * 2. The signal may increase and decrease several times before reaching it's peak amplitude
        * 3. A fixed threshold might not work for all microphones and devices
        * I will address these in this method
        */

        // 1. To fix problem one, we need to ignore spikes in amplitude
        // this requires measuring a few readings before and after each current reading
        // if the spike only lasts for one or two readings then it can be ignored.
        if(ca > higherThreshold)
        {
            ampCount++;
        }
        if(ca < lowerThreshold)
        {
            returnVal = ampCount*time;
            ampCount = 0;
        }

        return returnVal;
    }
}
