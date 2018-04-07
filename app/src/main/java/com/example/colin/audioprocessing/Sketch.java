package com.example.colin.audioprocessing;

/**
 * Created by Colin on 07/04/2018.
 */
import processing.core.PApplet;

public class Sketch extends PApplet {
    public void settings() {
        fullScreen();
    }

    public void setup() { }

    public void draw()
    {
        strokeWeight(5);

        for(int i = 0; i < height; i++)
        {
            stroke(0);
            fill(255);
            rect(0,80*i,160,80);
        }


        int count = 1;
        for(int i = 0; i < height; i++)
        {
            if(count < 4)
            {
                fill(0);
                rect(0,(50*(i +1)) + 30*i ,120,60);
            }
            else if(count > 4 && count <7)
            {
                fill(0);
                rect(0,(50*(i +1)) + 30*i ,120,60);
            }
            else if(count == 7)
            {
                count = 0;
            }

            count++;
        }

        for(int i = 0; i < width; i ++)
        {
            strokeWeight(1);
            stroke(0);
            line(160*i, 0, 160*i, height);
        }


    }
}