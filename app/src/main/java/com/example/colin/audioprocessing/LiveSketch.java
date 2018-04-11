package com.example.colin.audioprocessing;

/**
 * Created by Colin on 07/04/2018.
 */
import java.util.ArrayList;

import processing.core.PApplet;

public class LiveSketch extends PApplet
{
    float globalX = 0;
    float globalY = 0;

    //an array of all the notes from lowest to highest
    //will read the notes form the array of segmented notes
    //when there is a match, get the index from the array below
    //this is where the note should be placed
    //(0,0) is the top left co-ordinate so notes must descend from the highest
    String[] notes =
            {
                    "C8","B7","A#7","A7","G#7","G7","F#7","F7",
                    "E7","D#7","D7","C#7","C7","B6","A#6","A6",
                    "G#6","G6","F#6","F6","E6","D#6","D6","C#6",
                    "C6 ","B5","A#5","A5","G#5","G5","F#5","F5",
                    "E5","D#5","D5","C#5","C5","B4","A#4","A4",
                    "G#4","G4","F#4","F4","E4","D#4","D4","C#4",
                    "C4","B3","A#3","A3","G#3","G3","F#3","F3",
                    "E3","D#3","D3","C#3","C3","B2","A#2","A2",
                    "G#2","G2","F#2","F2","E2","D#2","D2","C#2",
                    "C2","B1","A#1","A1","G#1","G1","F#1","F1",
                    "E1","D#1","D1","C#1","C1","B0","A#0","A0"
            };
    String[] black = new String[36];
    String[] white = new String[52];

    //arraylist of black notes and they're position in the whole input
    public static ArrayList<Integer> inputBn = new ArrayList<Integer>();
    public static ArrayList<Integer> inputBp = new ArrayList<Integer>();

    public static ArrayList<Integer> inputWn = new ArrayList<Integer>();
    public static ArrayList<Integer> inputWp = new ArrayList<Integer>();

    public static ArrayList<Float> totalNoteOffset = new ArrayList<Float>();

    int total;
    float totalOffset;
    int offset;

    //settings is used for things that need to just be ran once
    public void settings()
    {
        fullScreen();
    }

    public void setup()
    {
        int whiteCount = 0;
        int blackCount = 0;
        //fill the black and white key array
        for(int i = 0; i < notes.length; i++)
        {
            if(notes[i].contains("#"))
            {
                black[blackCount] = notes[i];
                //System.out.println("black note: " + black[blackCount]);
                blackCount ++;
            }
            else
            {

                white[whiteCount] = notes[i];
                //System.out.println("white note: " + white[whiteCount]);
                whiteCount++;
            }
        }
    }

    //draw will be called at the monitors refresh rate
    public void draw()
    {
        //clear the screen with each refresh
        //otherwise, the screen will fil up with old datasfd
        background(200);

        String note = LivePianoRoll.note;

        System.out.println("note is this: " + note);
        //dividing lines
        for(int i = 0; i < width; i ++)
        {
            strokeWeight(1);
            stroke(0);
            line(160*i + globalX, 0, 160*i + globalX , height);
        }

        drawWhiteKeys(note);
        drawBlackKeys(note);
        mouseMoved();

        if(globalX > 0)
        {
            globalX = 0;
        }

        if(globalY <= -80*28)
        {
            globalY = -80*28;
        }

        if(globalY > 0)
        {
            globalY = 0;
        }
    }

    private void drawWhiteKeys(String note)
    {
        String n = note;
        //52 white keys
        for(int i = 0; i < 52; i++)
        {

            if (n != null && n.equals(white[i]) )
            {
                strokeWeight(1);
                stroke(0);
                fill(255,0,0);
                rect(160,80*i+ globalY,width-160,80);

            }
            strokeWeight(5);
            stroke(0);
            fill(255);
            rect(0,80*i+ globalY,160,80);
        }

        int octave = 8;
        for(int i = 0; i<52; i++)
        {

            if(i%7 == 0)
            {
                textSize(50);
                stroke(0);
                fill(0);
                text(Integer.toString(octave), 125, 80*i + 60 + globalY);
                octave --;
            }
        }
    }

    private void drawBlackKeys(String note)
    {
        //black keys
        int count = 0;
        int gap = 0;
        boolean flipFlop = true;
        String n = note;

        //only 36 black keys
        for(int i = 0; i < 36; i++)
        {
            if (n != null && n.equals(black[i]) )
            {
                stroke(0);
                fill(255,0,0);
                rect(160,80*i+ globalY,width-160,80);

            }
            if(flipFlop)
            {
                if(count < 3)
                {
                    fill(0);
                    rect(0,(50*(i + 1) + 80) + 30*i + globalY + gap,120,60);
                    count++;
                }
                if(count == 3)
                {
                    gap += 50 + 30;
                    count = 0;
                    flipFlop = false;
                }
            }
            else
            {
                if(count < 2)
                {
                    fill(0);
                    rect(0,(50*(i + 1) + 80) + 30*i + globalY + gap,120,60);
                    count++;
                }
                if(count == 2)
                {
                    gap += 50 + 30;
                    count = 0;
                    flipFlop = true;
                }
            }
        }
    }

    //this method will detect the direction of movement of the mouse
    //and will adjust the global X and Y values accordingly
    public void mouseMoved()
    {
        //float dx = mouseX - pmouseX;
        float dy = mouseY - pmouseY;


        /*if(globalX!=0)
        {
            //move everything to the right
            if(dx > 0)
            {
                globalX += dx;
            }

        }

        //move everything to the left
        if(dx < 0)
        {
            globalX +=dx;
        }*/

        if(globalY!=0)
        {
            //move everything down
            if(dy > 0)
            {
                globalY +=dy;
            }

        }

        //move everything up
        if(dy < 0)
        {
            globalY +=dy;
        }
    }
}