package cogs198.timeline;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.Date;


class Event {
    Date start;
    Date end;
    int priority;
    int position;
    int size;
    int gap = 20;
    int width, height;
    String title;
    Canvas canvas;

    Event(String setTitle, long setStart, long setEnd, int setPriority) {
        priority = setPriority;
        start = new Date(setStart);
        end = new Date(setEnd);
        title = setTitle;

        switch (priority) {

            case 0:
                size = 20;
                break;
            case 1:
                size = 35;
                break;
            case 2:
                size = 50;
                break;
        }





    }

    void draw(Canvas setCanvas, Paint paint, int setWidth, int setHeight, int offset) {

        canvas = setCanvas;
        width = setWidth;
        height = setHeight;

        position = (height/12)*start.getHours() + offset;

        int rectTop = position - size - gap;
        int rectBottom = position + size + gap;
        int rectLeft = width / 2 - 6 - 90;
        int rectRight = width / 2 + 6 - 90;


        //Typeface font = Typeface.create("Helvetica",Typeface.BOLD);
        // paint.setTypeface(font);



        paint.setColor(Color.parseColor("#c9e5e3"));
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);
        paint.setColor(Color.parseColor("#E5F3FF"));
        canvas.drawCircle(width/2 - 90, position, size, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(32);

        canvas.drawText(title, width / 2 + 60 - 90, position + 13, paint);
        canvas.drawText(Integer.toString(start.getHours()), width / 2 - 90 - 100, position + 13, paint);


    }

}