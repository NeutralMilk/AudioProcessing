package com.example.colin.audioprocessing;

/**
 * Created by Colin on 07/04/2018.
 */
import java.util.ArrayList;

import processing.core.PApplet;

public class PostSketch extends PApplet
{
    float globalX = 0;
    float globalY = 0;
    float time = 0.02321995464f;

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
    String[] inputNotes;
    Integer[] inputTimes;

    //arraylist of black notes and they're position in the whole input
    public static ArrayList<String> blackNotes = new ArrayList<String>();
    public static ArrayList<Float> blackNoteTimes = new ArrayList<Float>();

    public static ArrayList<String> whiteNotes = new ArrayList<String>();
    public static ArrayList<Float> whiteNoteTimes = new ArrayList<Float>();

    public static ArrayList<String> allNotes = new ArrayList<String>();
    public static ArrayList<Float> allNotetimes = new ArrayList<Float>();


    public static ArrayList<Float> totalNoteOffset = new ArrayList<Float>();

    int total;
    float totalOffset = 500;

    //settings is used for things that need to just be ran once
    public void settings()
    {
        fullScreen();
    }

    public void setup()
    {
        System.out.println(PostPianoRoll.notes);


        int count =0;
        for(int i  = 0; i < MainActivity.note_time.length; i++)
        {
            if(MainActivity.note_time[i][0] != null)
            {
                allNotes.add(MainActivity.note_time[i][0].toString());
                allNotetimes.add((int)MainActivity.note_time[i][1] * time);
                totalNoteOffset.add(totalOffset*allNotetimes.get(count));
                System.out.println("Z " + allNotes.get(count) + " | " + allNotetimes.get(count) + " | " + totalNoteOffset.get(count));
                count++;
            }
        }

        //fill the black and white key array
        for(int i = 0; i < allNotes.size(); i++)
        {
            if(allNotes.get(i).contains("#"))
            {
                blackNotes.add(allNotes.get(i));
                blackNoteTimes.add(allNotetimes.get(i));

            }
            else
            {
                whiteNotes.add(allNotes.get(i));
                whiteNoteTimes.add(allNotetimes.get(i));
            }
        }

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

        total = blackNotes.size() + whiteNotes.size();
        System.out.println("black white total " + blackNotes.size() + " " +whiteNotes.size() + " " + total);

    }

    //draw will be called at the monitors refresh rate
    public void draw()
    {
        //clear the screen with each refresh
        //otherwise, the screen will fil up with old datasfd
        background(200);



        //dividing lines
        for(int i = 0; i < width; i ++)
        {
            strokeWeight(1);
            stroke(0);
            line(160 + 500*i + globalX, 0, 160 + 500*i + globalX , height);
        }


        drawNotes();
        //drawWhiteNotes();
        //drawBlackNotes();
        drawWhiteKeys();
        drawBlackKeys();
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

    public void drawNotes()
    {
        totalOffset = 0;
        for(int i = 0; i < allNotes.size(); i++)
        {
            String currentNote = allNotes.get(i);
            //deal with black notes first
            if(currentNote.equals("-"))
            {
                totalOffset += totalNoteOffset.get(i);
            }
            else if(currentNote.contains("#"))
            {
                totalOffset += totalNoteOffset.get(i);
            }
            else
            {
                int count = 0;
                while(currentNote.equals(white[count]) == false)
                {
                    count++;
                }
                //System.out.println("made it this far");
                strokeWeight(5);
                stroke(0);
                fill(255,100,0);
                rect(totalOffset + globalX,80*count+ globalY,totalNoteOffset.get(i),80);
                totalOffset += totalNoteOffset.get(i);

                //when the while loop breaks, we have the location that the note needs to be placed
            }
        }
    }
    public void drawWhiteNotes()
    {
        totalOffset = 0;
        int change = 0;
        for(int i = 0; i < 52; i++)
        {
            for(int j = 0; j < allNotes.size(); j++)
            {

                strokeWeight(5);
                stroke(0);
                fill(255,change,0);
                if(allNotes.get(j).equals("-")!= true)
                {

                    if(white[i].equals(allNotes.get(j)))
                    {
                        System.out.println(allNotes.get(j));
                        rect(totalOffset + globalX,80*i+ globalY,totalNoteOffset.get(j),80);
                        try
                        {
                            if(allNotes.get(j-1).equals("-"))
                            {
                                totalOffset+=totalNoteOffset.get(j-1);
                            }
                        }
                        catch(Exception e)
                        {
                            if(j == 0)
                            {
                                totalOffset+=totalNoteOffset.get(j-1);
                            }
                        }

                        totalOffset += totalNoteOffset.get(j);
                        change += 20;
                    }

                }

            }

        }

    }

    public void drawBlackNotes()
    {
        //black keys
        int count = 0;
        int gap = 0;
        boolean flipFlop = true;

        //only 36 black keys
        for(int i = 0; i < 36; i++)
        {
            for(int j = 0; j < allNotes.size(); j++)
            {
                if(black[i].equals(allNotes.get(j)))
                {
                    if(flipFlop)
                    {
                        if(count < 3)
                        {
                            if(black[i].equals(allNotes.get(j)))
                            {
                                System.out.println(allNotes.get(j));
                                //rect(0,(50*(i + 1) + 80) + 30*i + globalY + gap,120,60);
                                rect(totalOffset + globalX,(50*(i + 1) ) + 30*i + globalY + gap,totalNoteOffset.get(j),60);
                                try
                                {
                                    if(allNotes.get(j-1).equals("-"))
                                    {
                                        totalOffset+=totalNoteOffset.get(j-1);
                                    }
                                }
                                catch(Exception e)
                                {
                                    if(j == 0)
                                    {
                                        totalOffset+=totalNoteOffset.get(j-1);
                                    }
                                }
                                totalOffset += totalNoteOffset.get(j);
                            }
                            if(count == 3)
                            {
                                gap += 50 + 30;
                                count = 0;
                                flipFlop = false;
                            }
                            count++;
                        }
                    }
                    else
                    {
                        if(count < 2)
                        {
                                System.out.println(allNotes.get(j));
                                rect(totalOffset + globalX,80*i+ globalY,totalNoteOffset.get(j),60);
                                try
                                {
                                    if(allNotes.get(j-1).equals("-"))
                                    {
                                        totalOffset+=totalNoteOffset.get(j-1);
                                    }
                                }
                                catch(Exception e)
                                {
                                    if(j == 0)
                                    {
                                        totalOffset+=totalNoteOffset.get(j-1);
                                    }
                                }

                                totalOffset += totalNoteOffset.get(j);
                        }
                        if(count == 2)
                        {
                            gap += 50 + 30;
                            count = 0;
                            flipFlop = true;
                        }
                        count++;
                    }
                }
            }
        }
    }


    public void drawWhiteKeys()
    {
        for(int i = 0; i < 52; i++)
        {
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

    public void drawBlackKeys()
    {
        //black keys
        int count = 0;
        int gap = 0;
        boolean flipFlop = true;

        //only 36 black keys
        for(int i = 0; i < 36; i++)
        {
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
        float dx = mouseX - pmouseX;
        float dy = mouseY - pmouseY;


        if(globalX!=0)
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
        }

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