package cogs198.timeline;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.Date;


class Event {
    Date start; //used to easily access day, hour, minute etc.
    Date end; //currently unused
    int priority;
    String title;
    long startEpoch; //used for actual time comparisons

    int position = 0;
    int size; //defined by priority
    int gapSize = 20;
    int minDistance = 60; //minimum event spacing
    int maxDistance = 1000; //maximum event spacing
    int hourSize = 70; //event spacing per hour

    int lowSize = 20; //low priority event size
    int medSize = 35; //medium priority event size
    int highSize = 50; //high priority event size

    static int width, height; //width and height of canvas

    static Canvas canvas;
    static Paint paint;
    static int nodeOffset = 0; //used for relative event spacing

    int rectColor = Color.parseColor("#c9e5e3");
    int eventColor;
    int textColor = Color.parseColor("#ffffff");
    int textSize = 32;

    Event prevNode = this; //previous event, default value for this event as first event
    Event nextNode = this; //next event, default value for this event as first event

    Event(String setTitle, long setStart, long setEnd, int setPriority, Event setPrevNode, Event setNextNode) {
        priority = setPriority;
        start = new Date(setStart);
        end = new Date(setEnd);
        title = setTitle;
        startEpoch = setStart;

        if (setPrevNode != null) //if this is not the first event
        {
            prevNode = setPrevNode;
            nextNode = setNextNode;
        }
        switch (priority) {
            case 0:
                size = lowSize;
                eventColor = Color.parseColor("#E5F3FF");
                break;
            case 1:
                size = medSize;
                eventColor = Color.parseColor("#E5F3FF");
                break;
            case 2:
                size = highSize;
                eventColor = Color.parseColor("#E5F3FF");
                break;
            default:
                size = highSize;
                eventColor = Color.parseColor("#E5F3FF");
                break;
        }
    }
    //this method is always called from the head node, it will never be called with no events
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
        if (setStart > startEpoch) //new event start > old event start
        {
            if (nextNode != head)
                return nextNode.addEvent(setTitle, setStart, setEnd, setPriority, head);

            else //added event is the latest event
            {
                nextNode = new Event(setTitle, setStart, setEnd, setPriority, this, head);
                return head;
            }
        }
        else //new event start <= old event start
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
    //set static variables, called once from head node
    void setCanvas(Canvas setCanvas, Paint setPaint, int setWidth, int setHeight) {
        canvas = setCanvas;
        width = setWidth;
        height = setHeight;
        paint = setPaint;
    }

    //initially called from head, called recursively until events are off the display
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

        int rectTop = position - size - gapSize;
        int rectBottom = position + size + gapSize;
        int rectLeft = width / 2 - 6 - 90;
        int rectRight = width / 2 + 6 - 90;

        //set up date text
        int startMin = start.getMinutes();
        String startText;
        if (startMin == 0) {
            startText = Integer.toString(start.getHours()) + ":" + "00";
        }
        else {
            startText = Integer.toString(start.getHours()) + ":" + Integer.toString(startMin);
        }

        //draw background color rectangle to make gap in timeline around ends of event
        paint.setColor(rectColor);
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);

        //draw event
        paint.setColor(eventColor);
        canvas.drawCircle(width/2 - 90, position, size, paint);

        //set text properties
        paint.setColor(textColor);
        paint.setTextSize(textSize);

        //draw title text on right hand side
        canvas.drawText(title, width / 2 + 60 - 90, position + 13, paint);

        //draw date text on left hand side
        canvas.drawText(startText, width / 2 - 90 - 120, position + 13, paint);

        if (position > height) //event is off the screen
            return;
        else if (nextNode != head) //this is not the last event
            nextNode.draw(offset, head);

    }

}