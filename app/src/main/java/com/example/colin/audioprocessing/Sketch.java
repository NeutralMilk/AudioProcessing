package com.example.colin.audioprocessing;

/**
 * Created by Colin on 07/04/2018.
 */
import processing.core.PApplet;

public class Sketch extends PApplet
{
    float globalX = 0;
    float globalY = 0;

    //an array of all the notes from lowest to highest
    //will read the notes form the array of segmented notes
    //when there is a match, get the index from the array below
    //this is where the note should be placed
    String[] notes =
            {
                    "A0","A#0/Bb0","B0","C1","C#1/Db1","D1","D#1/Eb1",
                    "E1","F1","F#1/Gb1","G1","G#1/Ab1","A1","A#1/Bb1",
                    "B1","C2","C#2/Db2","D2","D#2/Eb2","E2","F2","F#2/Gb2",
                    "G2","G#2/Ab2","A2","A#2/Bb2","B2","C3","C#3/Db3","D3",
                    "D#3/Eb3","E3","F3","F#3/Gb3","G3","G#3/Ab3","A3",
                    "A#3/Bb3","B3","C4","C#4/Db4","D4","D#4/Eb4","E4",
                    "F4","F#4/Gb4","G4","G#4/Ab4","A4","A#4/Bb4","B4",
                    "C5","C#5/Db5","D5","D#5/Eb5","E5","F5","F#5/Gb5",
                    "G5","G#5/Ab5","A5","A#5/Bb5","B5","C6 ","C#6/Db6",
                    "D6","D#6/Eb6","E6","F6","F#6/Gb6","G6","G#6/Ab6",
                    "A6","A#6/Bb6","B6","C7","C#7/Db7","D7","D#7/Eb7",
                    "E7","F7","F#7/Gb7","G7","G#7/Ab7","A7","A#7/Bb7",
                    "B7","C8"
            };
    public void settings() {
        fullScreen();
    }

    public void setup() { }

    public void draw()
    {
        background(200);

        //diving lines
        for(int i = 0; i < width; i ++)
        {
            strokeWeight(1);
            stroke(0);
            line(160*i + globalX, 0, 160*i + globalX , height);
        }

        //white keys
        strokeWeight(5);

        for(int i = 0; i < 52; i++)
        {
            stroke(0);
            fill(255);
            rect(0,80*i+ globalY,160,80);
        }


        //black keys
        int count = 1;
        for(int i = 0; i < 50; i++)
        {
            if(count < 4)
            {
                fill(0);
                rect(0,(50*(i +1) + 80) + 30*i + globalY ,120,60);
            }
            else if(count > 4 && count <7)
            {
                fill(0);
                rect(0 ,(50*(i +1) + 80) + 30*i + globalY,120,60);
            }
            else if(count == 7)
            {
                count = 0;

            }


            count++;
        }



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

    public void mouseMoved()
    {
        float dx = mouseX - pmouseX;
        float dy = mouseY - pmouseY;

        //dont let the piano move to the right!
        if(mouseX > 160)
        {
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
        }

        if(mouseX < 160 )
        {
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
}