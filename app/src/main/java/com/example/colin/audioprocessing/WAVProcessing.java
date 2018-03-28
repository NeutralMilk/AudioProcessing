package com.example.colin.audioprocessing;

import android.content.Context;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Colin on 27/03/2018.
 */

public class WAVProcessing
{

    Context context;
    byte[] bytes;
     static final int WINDOW_SIZE_PITCH    = 4096;
    private static final int WINDOW_SIZE_AMP    = 1024;
    private static final int WINDOW_OVERLAP_PITCH = WINDOW_SIZE_PITCH * 3/4;
    private Yin yin = null;
    ProcessAmplitude pa;
    ProcessNote pn;

    public void readWav()
    {
        String wavPath = MainActivity.context.getFilesDir() + "/" + "scale.wav";
        File wF = new File(wavPath);
        int size = wavPath.length();
        bytes = new byte[size];

        //Read the wav file into an input stream.
        //This will give me an array of bytes containing the raw data of the wav file
        //This isn't much use until it's converted to a short array however.
        try
        {
            InputStream wavFile = new BufferedInputStream(new FileInputStream(wF));
            System.out.println("works 1");

            int read;

            //2 everything because there are two bytes for every short.
            final int byteDiffPitch = WINDOW_SIZE_PITCH*2 - WINDOW_OVERLAP_PITCH*2;
            final int diffPitch = WINDOW_SIZE_PITCH - WINDOW_OVERLAP_PITCH;
            byte[] buff = new byte[8192];
            int count = 1;
            while ((read = wavFile.read(buff, (WINDOW_OVERLAP_PITCH*2)*(count), byteDiffPitch)) != -1)
            {
                count++;
                //out.write(buff, 0, read);
                //get my bytes into a short array
                final short[] sData = new short[WINDOW_SIZE_PITCH];
                final float[] fData = new float[WINDOW_SIZE_PITCH];

                //create a byte buffer to hold the bytes
                ByteBuffer bb = ByteBuffer.wrap(buff);

                //wav files use a little endian byte order
                bb.order( ByteOrder.LITTLE_ENDIAN);
                int i  = 0;
                //get the shorts from the byte buffer
                while( bb.hasRemaining())
                {
                    short v = bb.getShort();
                    sData[i] = v;
                    i++;
                }

                //YIN takes floats, convert to floats.
                for (int j = WINDOW_OVERLAP_PITCH; j < diffPitch; ++j)
                {
                    fData[j] = (float) sData[j];
                    Log.v(String.valueOf(fData[j]), "fdata");
                    System.out.println("fdata is" + fData[j]);
                }//end for

                //create an instance of ProcessNote and ProccessAmplitude
                pn = new ProcessNote();
                String note = pn.processNote(fData);

                pa = new ProcessAmplitude();
                double[] amplitude = pa.processAmplitude(sData);

                for (int j = 0; j < WINDOW_OVERLAP_PITCH; ++j)
                {
                    sData[j] = sData[j + diffPitch];
                    fData[j] = (float) sData[j + diffPitch];
                }//end for
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
