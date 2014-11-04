package cogs198.timeline;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.Date;


class Event {
    Date start;
    long startEpoch;
    Date end;
    long today = 1414369636;
    int priority;
    int position = 0;
    int size;
    int gap = 20;
    int minDistance = 60;
    int maxDistance = 1000;
    int hourSize = 70;

    int lowSize = 20;
    int medSize = 35;
    int highSize = 50;

    static int width, height;
    String title;

    static Canvas canvas;
    static Paint paint;
    static int nodeOffset = 0;

    Event prevNode = this;
    Event nextNode = this;

    Event(String setTitle, long setStart, long setEnd, int setPriority, Event setPrevNode, Event setNextNode) {
        today *= 1000;
        priority = setPriority;
        start = new Date(setStart);
        startEpoch = setStart;
        end = new Date(setEnd);
        title = setTitle;

        if (setPrevNode != null)
        {
            prevNode = setPrevNode;
            nextNode = setNextNode;
        }
        switch (priority) {

            case 0:
                size = lowSize;
                break;
            case 1:
                size = medSize;
                break;
            case 2:
                size = highSize;
                break;
            default: size = highSize;
                break;
        }
    }
    public Event addEvent(String setTitle, long setStart, long setEnd, int setPriority, Event head)
    {
        if (nextNode == this && prevNode == this) //only one Node
        {
            prevNode = new Event(setTitle, setStart, setEnd, setPriority, this, head);
            nextNode = prevNode;

            if (nextNode.startEpoch > startEpoch)
                return this;
            else
                return nextNode;
        }
        if (setStart > startEpoch)
        {
            if (nextNode != head)
                return nextNode.addEvent(setTitle, setStart, setEnd, setPriority, head);

            else //added event is the latest event
            {
                nextNode = new Event(setTitle, setStart, setEnd, setPriority, this, head);
                return head;
            }
        }
        else //setDate.startDate < startDate
        {
            Event oldPrev = prevNode;
            prevNode = new Event(setTitle, setStart, setEnd, setPriority, oldPrev, this);
            oldPrev.nextNode = prevNode;

            if (this == head)
                return prevNode;
            else
                return head;
        }
    }
    void setCanvas(Canvas setCanvas, Paint setPaint, int setWidth, int setHeight) {
        canvas = setCanvas;
        width = setWidth;
        height = setHeight;
        paint = setPaint;
    }

    void draw(int offset, Event head) {



        if (this == head) {
            position = 100 + offset;
        }
        else {
            position = (int)(prevNode.position +
                    (Math.abs(startEpoch -  prevNode.startEpoch)/ 3600000)*hourSize + nodeOffset);

            if(Math.abs(position - prevNode.position) <= minDistance) {
                nodeOffset += minDistance - Math.abs(position - prevNode.position);
                position = (int)(prevNode.position +
                        ((startEpoch -  prevNode.startEpoch)/3600000)*hourSize + nodeOffset);
            }
            else if(Math.abs(position - prevNode.position) >= maxDistance) {
                position = (int)(prevNode.position + 300 + nodeOffset);
            }
        }

        int rectTop = position - size - gap;
        int rectBottom = position + size + gap;
        int rectLeft = width / 2 - 6 - 90;
        int rectRight = width / 2 + 6 - 90;

        int startMin = start.getMinutes();
        String startText;
        if (startMin == 0) {
            startText = Integer.toString(start.getHours()) + ":" + "00";
        }
        else {
            startText = Integer.toString(start.getHours()) + ":" + Integer.toString(startMin);
        }
        //Typeface font = Typeface.create("Helvetica",Typeface.BOLD);
        // paint.setTypeface(font);

        paint.setColor(Color.parseColor("#c9e5e3"));
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);
        paint.setColor(Color.parseColor("#E5F3FF"));
        canvas.drawCircle(width/2 - 90, position, size, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(32);

        canvas.drawText(title, width / 2 + 60 - 90, position + 13, paint);
        canvas.drawText(startText, width / 2 - 90 - 120, position + 13, paint);

        if (nextNode != head)
            nextNode.draw(offset, head);

    }

}