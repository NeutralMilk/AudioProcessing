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
    static final int WINDOW_SIZE_SHORTS    = 4096;
    static final int WINDOW_SIZE_BYTES = 8192;
    private static final int WINDOW_OVERLAP_BYTES = WINDOW_SIZE_BYTES * 3/4;
    private static final int WINDOW_OVERLAP_SHORTS = WINDOW_SIZE_SHORTS * 3/4;

    ProcessAmplitude pa;
    ProcessNote pn;
    Segmentation segment;
    String note;
    int count = 0;

    public void readWav()
    {
        String wavPath = MainActivity.context.getFilesDir() + "/" + "scale.wav";
        final File wF = new File(wavPath);
        int size = wavPath.length();
        bytes = new byte[size];
        pn = new ProcessNote();
        pa = new ProcessAmplitude();
        segment = new Segmentation();
        //Read the wav file into an input stream.
        //This will give me an array of bytes containing the raw data of the wav file
        //This isn't much use until it's converted to a short array however.
        final short[] sData = new short[WINDOW_SIZE_SHORTS];
        final float[] fData = new float[WINDOW_SIZE_SHORTS];
        try
        {
            InputStream wavFile = new BufferedInputStream(new FileInputStream(wF));
            System.out.println("works 1");

            int read;

            //2 everything because there are two bytes for every short.
            //final int byteDiffPitch = WINDOW_SIZE_PITCH * 2 - WINDOW_OVERLAP_PITCH * 2;
            byte[] buff = new byte[WINDOW_SIZE_BYTES];
            int diffByte = WINDOW_SIZE_BYTES - WINDOW_OVERLAP_BYTES;
            int diffShort = WINDOW_OVERLAP_SHORTS - WINDOW_OVERLAP_SHORTS;


            //first write diffByte amount of bytes into the WINDOW_OVERLAP_BYTES position of the buffer
            //this will fill the last 1/4 of the buffer
            //this will then each '1/4' segment of the buffer will be moved back 1/4
            //this allows me to fill a buffer of size 4096 but make readings 4 times faster
            while ((read = wavFile.read(buff, WINDOW_OVERLAP_BYTES, diffByte)) != -1)
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
                    sData[i] = v;
                    fData[i] = (float) sData[i];
                    i++;
                }


                note = pn.processNote(fData);

                double amplitude = pa.processAmplitude(sData);

                //print out to see my results
                System.out.println("Note & amplitude " + note + " | " + amplitude);
                //System.out.println("A" + " is " + amplitude);

                float valid = segment.segmentation(note, amplitude);
                //this will move each quarter back one quarter
                for (i = 0; i < WINDOW_OVERLAP_BYTES; ++i)
                {
                    buff[i] = buff[i + diffByte];
                }//end for*/
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