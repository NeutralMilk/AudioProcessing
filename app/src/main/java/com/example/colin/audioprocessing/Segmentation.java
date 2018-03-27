package com.example.colin.audioprocessing;

/**
 * Created by Colin on 27/03/2018.
 */

public class Segmentation {

    String previousNote;
    String currentNote;
    long currentTime;
    long previousTime;
    Short[] audioShorts;
    int[] amplitudes = new int[4];
    String note;

}
