package com.example.colin.audioprocessing;

/**
 * Created by Colin on 27/03/2018.
 */

public class ProcessAmplitude
{

    short[] audioShorts;

    public double processAmplitude(short[] audioShorts)
    {
        this.audioShorts = audioShorts;
        double amplitude;

        double sum = 0;
        for(int i = 0; i < audioShorts.length; i++)
        {
            sum += audioShorts [i] * audioShorts [i];
        }//end for

        amplitude = sum / 1024;
        amplitude = Math.sqrt(amplitude);

        return amplitude;
    }
}
