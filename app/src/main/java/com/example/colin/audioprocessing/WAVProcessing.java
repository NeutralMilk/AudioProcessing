package com.example.colin.audioprocessing;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.content.Context.MODE_APPEND;

/**
 * Created by Colin on 27/03/2018.
 */

public class WAVProcessing {

    private void readWav()
    {
        String wavPath = MainActivity.this.getFilesDir() + "/" + "scale.wav";
        File wF = new File(wavPath);
        int size = wavPath.length();
        bytes = new byte[size];

        byte[] audioBytes = null;

        //Read the wav file into an input stream, and then copy this into a ByteArrayOutputStream.
        //This will give me an array of bytes containing the raw data of the wav file
        //This isn't much use until it's converted to a short array however.
        try
        {
            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream wavFile = new BufferedInputStream(new FileInputStream(wF));
            System.out.println("works 1");
            System.out.println(s);

            int read;

            //2 everything because there are two bytes for every short.
            final int byteDiffPitch = WINDOW_SIZE_PITCH*2 - WINDOW_OVERLAP_PITCH*2;
            final int diffPitch = WINDOW_SIZE_PITCH - WINDOW_OVERLAP_PITCH;
            byte[] buff = new byte[8192];
            int count = 1;
            while ((read = wavFile.read(buff, (WINDOW_OVERLAP_PITCH*2)*//**//**(count)*//**//*, byteDiffPitch)) != -1)
            {
                count++;
                //out.write(buff, 0, read);
                //get my bytes into a short array
                final short[] sDataPitch = new short[WINDOW_SIZE_PITCH];
                final float[] fData = new float[WINDOW_SIZE_PITCH];
                final short[] sDataAmp = new short[WINDOW_SIZE_AMP];
                ByteBuffer bb = ByteBuffer.wrap(buff);
                bb.order( ByteOrder.LITTLE_ENDIAN);
                int i  = 0;
                while( bb.hasRemaining())
                {
                    short v = bb.getShort();
                    sDataPitch[i] = v;
                    i++;
                }

                for (int j = WINDOW_OVERLAP_PITCH; j < diffPitch; ++j)
                {
                    fData[j] = (float) sDataPitch[j];
                    Log.v(String.valueOf(fData[j]), "fdata");
                    System.out.println("fdata is" + fData[j]);
                }//end for

                float currentPitch = yin.getPitch(fData).getPitch();

                pitch = currentPitch;

                printNote = updateNote(pitch);

                for (int j = 0; j < WINDOW_OVERLAP_PITCH; ++j)
                {
                    sDataPitch[j] = sDataPitch[j + diffPitch];
                    fData[j] = (float) sDataPitch[j + diffPitch];
                }//end for

                double amplitude[] = new double[4];

                for(int j = 0 ; j < 4; j++)
                {
                    double sum = 0;
                    for(int k = 0; k < 1024; k++)
                    {
                        sum += sDataPitch [k*(j+1)] * sDataPitch [k*(j+1)];
                    }//end for

                    boolean valid = false;
                    if (1024 >= 0)
                    {
                        amplitude[j] = sum / 1024;
                        try
                        {
                            out = new OutputStreamWriter(openFileOutput("save.txt", MODE_APPEND));
                            out.write(Integer.toString((int) Math.sqrt(amplitude[j])) + " | " + pitch + "Hz | " + printNote);
                            out.write("\r\n");
                            out.close();
                        }
                        catch(IOException e)
                        {
                            System.out.println("not working");
                        }

                        if(j == 0)
                        {
                            System.out.println(Integer.toString((int) Math.sqrt(amplitude[j])) + " | " + pitch + "Hz | " + printNote);
                        }
                        else
                        {
                            System.out.println(Integer.toString((int) Math.sqrt(amplitude[j])));
                        }
                        amplitude[j] = Math.sqrt(amplitude[j]);
                        valid = segmentation(amplitude, pitch);
                    }//end if
                }
            }

            out.flush();
            audioBytes = out.toByteArray();
            //print this out to check that there is the right number of bytes.
            System.out.println("there are "+ audioBytes.length + "Bytes");

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


        try
        {
            AudioTrack audioTrack = new  AudioTrack(AudioManager.STREAM_VOICE_CALL, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 4096, AudioTrack.MODE_STATIC);
            audioTrack.write(audioBytes, 0, 4096);
            audioTrack.play();

        } catch(Throwable t){
            Log.d("Audio","Playback Failed");
        }
    }
}
