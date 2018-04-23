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
import java.util.ArrayList;


public class PostProcessing extends AppCompatActivity
{
    byte[] bytes;
    static final int WINDOW_SIZE_SHORTS    = 4096;
    static final int WINDOW_SIZE_BYTES = 8192;
    private static final int WINDOW_OVERLAP_BYTES = WINDOW_SIZE_BYTES * 3/4;
    private static final int WINDOW_OVERLAP_SHORTS = WINDOW_SIZE_SHORTS * 3/4;

    ProcessAmplitude pa;
    ProcessNote pn;
    String note;
    int count = 0;
    Segmentation segment;
    static InputStream wF;
    public static ArrayList<String> noteList = new ArrayList<String>();
    public static ArrayList<Double> amplitudes = new ArrayList<Double>();


    public Object[][] readWav(InputStream wF)
    {
        this.wF = wF;
        segment = new Segmentation();

        //Read the wav file into an input stream.
        //This will give me an array of bytes containing the raw data of the wav file
        //This isn't much use until it's converted to a short array however.
        final short[] sData = new short[WINDOW_SIZE_SHORTS];
        final float[] fData = new float[WINDOW_SIZE_SHORTS];
        try
        {
            InputStream wavFile = wF;

            int read;

            //2 everything because there are two bytes for every short.
            //final int byteDiffPitch = WINDOW_SIZE_PITCH * 2 - WINDOW_OVERLAP_PITCH * 2;
            byte[] buff = new byte[WINDOW_SIZE_BYTES];
            int diffByte = WINDOW_SIZE_BYTES - WINDOW_OVERLAP_BYTES;

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
                    sData[i] = v;
                    fData[i] = (float) sData[i];
                    i++;
                }

                pn = new ProcessNote();
                note = pn.processNote(fData);
                noteList.add(note);

                pa = new ProcessAmplitude();
                double amp = pa.processAmplitude(sData);
                amplitudes.add(amp);

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

        Segmentation segment = new Segmentation();
        String[] notes = new String[noteList.size()];
        Double[] amps = new Double[amplitudes.size()];

        for(int i = 0; i < noteList.size(); i++)
        {
            notes[i] = noteList.get(i);
            amps[i] = amplitudes.get(i);
        }
        Object[][] note_time= new Object[noteList.size()][2];

        note_time = segment.segmentation(notes, amps);

        return note_time;
    }
}