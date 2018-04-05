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
    static final int WINDOW_SIZE_SHORTS    = 4096;
    static final int WINDOW_SIZE_BYTES = 8192;
    private static final int WINDOW_OVERLAP_BYTES = WINDOW_SIZE_BYTES * 3/4;
    private static final int WINDOW_OVERLAP_SHORTS = WINDOW_SIZE_SHORTS * 3/4;

    ProcessAmplitude pa;
    ProcessNote pn;
    String note;
    double amp;
    Segmentation segment;
    int noteCount = 0;
    int ampCount = 0;

    public void readWav()
    {
        String wavPath = MainActivity.context.getFilesDir() + "/" + "scale.wav";
        final File wF = new File(wavPath);
        segment = new Segmentation();
        //Read the wav file into an input stream.
        //This will give me an array of bytes containing the raw data of the wav file
        //This isn't much use until it's converted to a short array however.
        final short[] sData = new short[WINDOW_SIZE_SHORTS];
        final float[] fData = new float[WINDOW_SIZE_SHORTS];
        final short[] amplitudeData = new short[WINDOW_SIZE_SHORTS/4];
        byte[] buff = new byte[WINDOW_SIZE_BYTES];
        int diffByte = WINDOW_SIZE_BYTES - WINDOW_OVERLAP_BYTES;

        pn = new ProcessNote();
        pa = new ProcessAmplitude();

        try
        {
            InputStream wavFile = new BufferedInputStream(new FileInputStream(wF));

            //first write diffByte amount of bytes into the WINDOW_OVERLAP_BYTES position of the buffer
            //this will fill the last 1/4 of the buffer
            //this will then each '1/4' segment of the buffer will be moved back 1/4
            //this allows me to fill a buffer of size 4096 but make readings 4 times faster
            while ((wavFile.read(buff, WINDOW_OVERLAP_BYTES, diffByte)) != -1)
            {

                //create a byte buffer to hold the bytes
                ByteBuffer bb = ByteBuffer.wrap(buff);
                //wav files use a little endian byte order
                bb.order(ByteOrder.LITTLE_ENDIAN);
                int i = 0;
                //get the shorts from the byte buffer
                while (bb.hasRemaining())
                {
                    short v = bb.getShort();
                    if(i >= WINDOW_OVERLAP_SHORTS)
                    {
                        amplitudeData[i-WINDOW_OVERLAP_SHORTS] = sData[i];
                    }

                    sData[i] = v;
                    fData[i] = (float) sData[i];
                    i++;
                }

                note = pn.processNote(fData);
                amp = pa.processAmplitude(amplitudeData);

                //print out to see my results
                /*System.out.println("Note is " + note);
                System.out.println("A" + " is " + amplitude);*/

                //this will move each quarter back one quarter
                for (i = 0; i < WINDOW_OVERLAP_BYTES; ++i)
                {
                    buff[i] = buff[i + diffByte];
                }//end for*/

                float valid = segment.segmentation(note, amp);

                if(valid > 0)
                {
                    System.out.println("Note is " + note + " and it lasted for " + valid + " seconds");
                    noteCount = 0;
                }
                
            }
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