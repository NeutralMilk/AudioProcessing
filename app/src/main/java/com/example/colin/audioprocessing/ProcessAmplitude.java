package com.example.colin.audioprocessing;

/**
 * Created by Colin on 27/03/2018.
 */

public class ProcessAmplitude
{

    short[] audioShorts;

    public double[] processAmplitude(short[] audioShorts)
    {
        this.audioShorts = audioShorts;
        double[] amplitude = new double[4];

        for(int j = 0 ; j < 4; j++)
        {
            double sum = 0;
            for(int i = 0; i < 1024; i++)
            {
                sum += audioShorts [i*(j+1)] * audioShorts [i*(j+1)];
            }//end for

            amplitude[j] = sum / 1024;
            amplitude[j] = Math.sqrt(amplitude[j]);
        }
        return amplitude;
    }
}
