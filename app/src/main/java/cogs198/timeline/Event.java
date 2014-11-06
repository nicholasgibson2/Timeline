package cogs198.timeline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.WindowManager;

import java.util.Date;


class Event {

    Date start; //used to easily access day, hour, minute etc.
    Date end; //currently unused
    int priority;
    String title;
    long startEpoch; //used for actual time comparisons

    int position = 0;
    int size; //defined by priority

    static int numEvents = 0;

    //used for getting screen dimensions
    WindowManager wm = (WindowManager) Timeline.holyContext.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    int width = display.getWidth(); //width of screen
    int height = display.getHeight(); //height of screen

    final int gapSize = height / 35; //size of timeline gap surrounding events
    final double headBuffer = height / 6.5; //buffer for first event from top of screen
    final int minDistance = height / 11; //minimum event spacing
    final double maxDistance = height * 1.5; //maximum event spacing
    final double maxNew = height * .8; //new spacing for events too far apart
    final int hourSize = height / 11; //event spacing per hour
    final int lowSize = height / 44; //low priority event size
    final int medSize = height / 24; //medium priority event size
    final int highSize = height / 18; //high priority event size
    final int rectSize = height / 133; //width of timeline, size of gap boxes
    final int leftShift = height / 10; //distance to shift left

    static Canvas canvas;
    static Paint paint;
    static int nodeOffset = 0; //used for relative event spacing

    int rectColor = Color.parseColor("#c9e5e3");
    int eventColor;
    int textColor = Color.parseColor("#ffffff");
    int topColor = Color.parseColor("#c1d9de");
    int textSize = 30;

    Event prevNode = this; //previous event, default value for this event as first event
    Event nextNode = this; //next event, default value for this event as first event

    //set canvas and paint, called once from head node
    void setCanvas(Canvas setCanvas, Paint setPaint) {
        canvas = setCanvas;
        paint = setPaint;
    }

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
        numEvents++;
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


    //initially called from head, called recursively until events are off the display
    void draw(int offset, Event head) {

        int epochHour = 3600000;

        if (this == head) {
            position = (int)headBuffer + offset;
        }
        else {
            position = (int)(prevNode.position +
                    (Math.abs(startEpoch -  prevNode.startEpoch)/epochHour)*hourSize + nodeOffset);

            //too close together
            if(Math.abs(position - prevNode.position) <= minDistance) {
                nodeOffset += minDistance - Math.abs(position - prevNode.position);
                position = (int)(prevNode.position +
                        ((startEpoch -  prevNode.startEpoch)/epochHour)*hourSize + nodeOffset);
            }
            //too far apart
            else if(Math.abs(position - prevNode.position) >= maxDistance) {
                position = (int)(prevNode.position + maxNew + nodeOffset);

               // paint.setColor(rectColor);
                //canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);
            }
        }
        if (position >= 0) {

            //set up rectangles
            int rectTop = position - size - gapSize;
            int rectBottom = position + size + gapSize;
            int rectLeft = width / 2 - rectSize - leftShift;
            int rectRight = width / 2 + rectSize - leftShift;

            //set up date text
            int startMin = start.getMinutes();
            String startText;

            startText = EventDate.convertTime(start.getHours(), start.getMinutes());

            //draw background color rectangle to make gap in timeline around ends of event
            paint.setColor(rectColor);
            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);

            //draw event
            paint.setColor(eventColor);
            canvas.drawCircle(width / 2 - leftShift, position, size, paint);

            //set text properties
            paint.setColor(textColor);
            paint.setTextSize(textSize);

            //draw title text on right hand side
            canvas.drawText(title, width / 2 + 70 - 90, position + 13, paint);

            //draw date text on left hand side
            canvas.drawText(startText, width / 2 - 90 - 140, position + 13, paint);
        }

        //if this is not the last event, and the event is not off the screen
        if ((nextNode != head) && (position < height))
            nextNode.draw(offset, head);
        else {
            //draw background color rectangle to make gap in timeline around ends of event
            paint.setColor(topColor);
            canvas.drawRect(0, 0, width, (int)(headBuffer*.65), paint);

            String dateText;

            if (prevNode.position >= (int)headBuffer) {
                dateText = EventDate.getDayShort(prevNode.start.getDay()) + ", " +
                    EventDate.getMonthLong(prevNode.start.getMonth()) + " " +
                    prevNode.start.getDate();
            }
            else {
                dateText = EventDate.getDayShort(start.getDay()) + ", " +
                        EventDate.getMonthLong(start.getMonth()) + " " +
                        start.getDate();
            }

            //set text properties
            paint.setColor(textColor);
            paint.setTextSize(50);
            canvas.drawText(dateText, 20, 50, paint);

            canvas.drawRect(0, (int)(headBuffer*.6), width, (int)(headBuffer*.65), paint);
        }



    }

}
class EventDate {

    public final static String[] monthLong = {"January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November", "December"};

    public final static String[] dayShort = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public static String getMonthLong(int month) {
            return monthLong[month];
        }
    public static String getDayShort(int day) {
        return dayShort[day];
    }
    public static String convertTime(int hour, int minute) {
        String twelveHour;
        boolean am = true;

        if (hour > 12) {
            hour -= 12;
            am = false;
        }
        else if (hour == 0)
            hour = 12;
        else if (hour == 12)
            am = false;

        twelveHour = Integer.toString(hour) + ":" + Integer.toString(minute);

        if (minute == 0)
            twelveHour += "0";

        if (am)
            twelveHour += "a";
        else
            twelveHour += "p";

        return twelveHour;
    }
}

