package com.example.colin.audioprocessing;

/**
 * Created by Colin on 07/04/2018.
 */
import processing.core.PApplet;

public class Sketch extends PApplet
{
    float globalX = 0;
    float globalY = 0;
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