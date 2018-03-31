package com.example.colin.audioprocessing;

import android.support.v7.app.AppCompatActivity;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class WAVProcessing extends AppCompatActivity
{
    byte[] bytes;
    static final int WINDOW_SIZE_PITCH    = 4096;
    private static final int WINDOW_OVERLAP_PITCH = WINDOW_SIZE_PITCH * 3/4;
    ProcessAmplitude pa;
    ProcessNote pn;
    String note;
    int count = 0;

    public void readWav()
    {
        String wavPath = MainActivity.context.getFilesDir() + "/" + "scale.wav";
        final File wF = new File(wavPath);
        int size = wavPath.length();
        bytes = new byte[size];

        //Read the wav file into an input stream.
        //This will give me an array of bytes containing the raw data of the wav file
        //This isn't much use until it's converted to a short array however.
        final short[] sData = new short[WINDOW_SIZE_PITCH];
        final float[] fData = new float[WINDOW_SIZE_PITCH];
        try
        {
            InputStream wavFile = new BufferedInputStream(new FileInputStream(wF));
            System.out.println("works 1");

            int read;

            //2 everything because there are two bytes for every short.
            //final int byteDiffPitch = WINDOW_SIZE_PITCH * 2 - WINDOW_OVERLAP_PITCH * 2;
            byte[] buff = new byte[2048];

            int streamRelOffset = 0;
            int streamAbsOffset = 0; //THIS IS JUST FOR TESTING PURPOSES
            //int overlap = WINDOW_OVERLAP_PITCH;
            while ((read = wavFile.read(buff, 0, 2048)) != -1)
            {
                streamAbsOffset += read;//THIS  HOLDS THE RELATIVE OFFSET INTO THE STREAM
                streamRelOffset += read; //THIS HOLDS THE RELATIVE OFFSET INTO THE STREAM
                count++;

                //THE OFFSET INTO THE BUFFER SHOULD NOT BE GREATER THAN  streamOffset,
                //OTHERWISE, WE'RE GOING TO SKIP SOME STREAM BYTES, WHICH WE DON'T WANT
                /*if(overlap*2*count>streamRelOffset)
                {
                    overlap = overlap*2*count-streamRelOffset;
                }*/

                //create a byte buffer to hold the bytes
                ByteBuffer bb = ByteBuffer.wrap(buff);

                //wav files use a little endian byte order
                bb.order(ByteOrder.LITTLE_ENDIAN);
                int i = 0;
                //get the shorts from the byte buffer
                while (bb.hasRemaining())
                {
                    short v = bb.getShort();
                    sData[i] = v;
                    fData[i] = (float) sData[i];
                    i++;
                }

                pn = new ProcessNote();
                note = pn.processNote(fData);

                pa = new ProcessAmplitude();
                double amplitude = pa.processAmplitude(sData);

                //print out to see my results
                System.out.println("Note is " + note);
                System.out.println("A" + " is " + amplitude);

                //MAKE SURE THAT WE DON'T CAUSE AN (ARRAY)INDEXOUTOFBOUNDSEXCEPTION
               /* if(byteDiffPitch > buff.length - overlap*2*count)
                {
                    //RESET THE COUNTER AND THE STREAM OFFSET
                    count = 0;
                    streamRelOffset = 0;
                }*/

            }
            System.out.println("ABSOLUTE OFFSET: "+streamAbsOffset);
        }

        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}