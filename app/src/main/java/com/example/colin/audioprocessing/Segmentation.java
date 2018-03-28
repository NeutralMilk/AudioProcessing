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
    int[] amplitudes = new int[4];
    String note;

    /*
    * A sample rate of 44.1kHz and a window size of 4096 will give 10.766 readings per second
    * Counting the number of readings taken can then be used to determine the time taken
    * 1 reading would take 1/10.766 or 0.09288 seconds
    * For amplitude readings there are 4 times as many readings because the window size is 1024
    * one amplitude reading will take 0.02322 seconds
    * this is more reliable than using a timer as it very easily produces incorrect readings, form my testing.
    */

    float pitchTime = 0.09287981859f;
    float ampTime = 0.02321995464f;
}
