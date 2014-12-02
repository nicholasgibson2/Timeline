package cogs198.timeline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.WindowManager;

import java.util.Date;


class Event {

    /************************************calendar event attributes*********************************/
    Date start; //used to easily access day, hour, minute etc.
    Date end; //currently unused
    int priority; //low, medium or high priority, determines size of circle and text
    String title; //title of event
    long startEpoch; //used for actual time comparisons
    long endEpoch; //used to display event tail/end time
    int position = 0; //position on canvas of event circle
    int size; //size of circle, defined by priority
    boolean repeated = false; //currently unused
    /**********************************************************************************************/

    /************************************misc setup************************************************/
    //used for getting screen dimensions
    final static WindowManager wm = (WindowManager) Timeline.holyContext.getSystemService(Context.WINDOW_SERVICE);
    final static Display display = wm.getDefaultDisplay();
    final static int width = display.getWidth(); //width of screen
    final static int height = display.getHeight(); //height of screen

    final static int epochMinute = 60000; //used to convert epoch time to minutes
    static Canvas canvas;
    static Paint paint;
    static int numEvents = 0; //used for debugging
    final static long today = System.currentTimeMillis(); //current time
    static int curBarPosition; //bar for current time
    /**********************************************************************************************/

    /************************************user interface********************************************/
    //spacing
    final static double headBuffer = height / 6.5; //buffer for first event from top of screen
    final static int minDistance = height / 11; //minimum event spacing
    final static double maxDistance = height*.4; //maximum event spacing
    final static double minuteSize = height / 660; //event spacing per minute
    final static int leftShift = height / 8; //x position of timeline and events

    //event sizes
    final static int lowSize = height / 44; //low priority event size
    final static int medSize = height / 24; //medium priority event size
    final static int highSize = height / 18; //high priority event size
    final static int tailSize = height / 133; //width of timeline, size of gap boxes

    //positioning for event titles and start times
    final static int titleX = width / 2 - height / 15;
    final static int titleY = height / 61;
    final static int startX = (int)(height / 80);
    final static int startY = titleY;

    //colors
    int tailColor = Color.parseColor("#b0c6dd"); //in case we want different color for tail
    int eventColor = Color.parseColor("#b0c6dd"); //will be set to lowColor, medColor, or highColor

    //will be used if we want different colors for priority
    /*final static int lowColor;
    final static int medColor;
    final static int highColor;*/
    final static int textColor = Color.parseColor("#000000"); //color of all text
    final static int white = Color.parseColor("#ffffff");
    final static int curBarColor = Color.parseColor("#FF0000"); //color of current time bar

    //text sizes
    final static int dateLabelSize = (int)(35 * Timeline.screenDensity);
    final static int lowTextSize = (int)(16 * Timeline.screenDensity); //low priority title size
    final static int medTextSize = (int)(20 * Timeline.screenDensity); //med priority title size
    final static int highTextSize = (int)(24 * Timeline.screenDensity); //high priority title size
    final static int leftTextSize = lowTextSize; //size of start time, same size as low priority

    int textSize = (int)(20 * Timeline.screenDensity); //set based on priority


    final static int curBarSize = (int)(2*Timeline.screenDensity); //size of current time bar
    /**********************************************************************************************/


    /************************************node data*************************************************/
    static int curHeadSpot = 0; //position of current head node

    //low med high nodes not set up yet, will be used for day/week/month views
    Event prevNodeLow = null; //previous event
    Event nextNodeLow = null; //next event

    Event prevNodeMed = null; //previous event
    Event nextNodeMed = null; //next event

    Event prevNodeHigh = null; //previous event
    Event nextNodeHigh = null; //next event

    Event prevNode = null; //previous event
    Event nextNode = null; //next event
    /**********************************************************************************************/

    //makes the head node/first displayed event the next upcoming event
    //called recursively
    Event updateHead(Event oldHead) {
        if (nextNode == null) //this is the last node
            return oldHead; //don't update head
        else if (startEpoch <= today) //next node start was before today
            return nextNode.updateHead(oldHead);
        else
            return this;
    }

    Event(String setTitle, long setStart, long setEnd, int setPriority, Event setPrevNode, Event setNextNode) {

        //set up event attributes
        priority = setPriority;
        start = new Date(setStart);
        end = new Date(setEnd);
        title = setTitle;
        startEpoch = setStart;
        endEpoch = setEnd;

        prevNode = setPrevNode;
        nextNode = setNextNode;

        //set size based on priority
        switch (priority) {
            case 0:
                size = lowSize;
                textSize = lowTextSize;
                //eventColor = lowColor;
                break;
            case 1:
                size = medSize;
                textSize = medTextSize;
                //eventColor = medColor;
                break;
            case 2:
                size = highSize;
                textSize = highTextSize;
                //eventColor = highColor;
                break;
            default:
                size = lowSize;
                //eventColor = lowColor;
                break;
        }
        tailColor = eventColor; //in case we want different color for tail
        numEvents++;
    }
    //adds a new Event node to the linked list, ordered by start time
    //this method is always called from the head node, it will never be called with zero events
    public Event addEvent(String setTitle, long setStart, long setEnd, int setPriority, Event head)
    {
        if (nextNode == null && prevNode == null) //only one Node
        {
            if (setStart > startEpoch) { //new event comes chronologically after head
                nextNode = new Event(setTitle, setStart, setEnd, setPriority, this, null);
                return this;
            }
            else { //new event comes chronologically before head
                prevNode = new Event(setTitle, setStart, setEnd, setPriority, null, this);
                return prevNode;
            }
        }
        else if (setStart > startEpoch) //new event start > old event start
        {
            if (nextNode != null) //new event is not the last event
                return nextNode.addEvent(setTitle, setStart, setEnd, setPriority, head);

            else //new event is the last event
            {
                nextNode = new Event(setTitle, setStart, setEnd, setPriority, this, null);
                return head;
            }
        }
        else //new event start <= old event start
        {
            Event oldPrev = prevNode;
            prevNode = new Event(setTitle, setStart, setEnd, setPriority, oldPrev, this);

            if (oldPrev != null)
                oldPrev.nextNode = prevNode;

            if (this == head)
                return prevNode;
            else
                return head;
        }
    }
    //initially called from head, called recursively until events are off the display
    //there is usually one node 'displayed' above of the top and one below the bottom of the screen
    void draw(int offset, Event head, boolean first) {

        String dateText; //used for formatting the date text

        //only true once, for setting up head position for the first time
        if (first) {
            position = (int) headBuffer + offset;
            curHeadSpot = position - offset;

            /***date label***/
            paint.setColor(white);
            canvas.drawRect(0, position - (int) (headBuffer), width,
                    position - (int) (headBuffer / 2), paint);

            dateText = EventText.getDayShort(start.getDay()) + " | " +
                    EventText.getMonthShort(start.getMonth()) + " " + start.getDate();

            paint.setColor(textColor);
            paint.setTextSize(dateLabelSize);
            canvas.drawText(dateText, height / 40, position - (int) (headBuffer / 1.5), paint);
            /****************/
        }
        //if this is the head node
        else if (this == head) {
            position = curHeadSpot + offset;

            /***date label***/
            if (prevNode == null) {
                paint.setColor(white);
                canvas.drawRect(0, position - (int) (headBuffer), width,
                        position - (int) (headBuffer / 2), paint);

                dateText = EventText.getDayShort(start.getDay()) + " | " +
                        EventText.getMonthShort(start.getMonth()) + " " + start.getDate();

                paint.setColor(textColor);
                paint.setTextSize(dateLabelSize);
                canvas.drawText(dateText, height / 40, position - (int) (headBuffer / 1.5), paint);
            }
            /****************/

            //node after head is off top of screen
            if (nextNode != null && nextNode.position <= 0) {
                Timeline.head = nextNode;
                int nextPosition = (int) (position + (Math.abs(nextNode.startEpoch - startEpoch)
                        / epochMinute) * minuteSize);

                //too close together
                if (Math.abs(nextPosition - position) < minDistance)
                    nextPosition = (int) (position + minDistance + 1);

                //too far apart
                else if (Math.abs(nextPosition - position) >= maxDistance)
                    nextPosition = (int) (position + maxDistance + 1);

                curHeadSpot = nextPosition - offset;

                nextNode.draw(offset, nextNode, false);
                return;
            }
            //checks if previous node should be displayed and made the new head
            else if (prevNode != null)
            {
                int prevPosition = (int) (position - (Math.abs(startEpoch - prevNode.startEpoch) /
                        epochMinute) * minuteSize);

                //too close together
                if (Math.abs(prevPosition - position) < minDistance)
                    prevPosition = (int) (position - minDistance - 1);

                //too far apart
                else if (Math.abs(prevPosition - position) >= maxDistance) {
                    prevPosition = (int) (position - maxDistance - 1);

                    /***date label***/
                    paint.setColor(white);
                    canvas.drawRect(0, position - (int)(maxDistance*.65), width,
                            position - (int)(maxDistance*.45), paint);

                    dateText = EventText.getDayShort(start.getDay()) + " | " +
                            EventText.getMonthShort(start.getMonth()) + " " + start.getDate();

                    paint.setColor(textColor);
                    paint.setTextSize(dateLabelSize);
                    canvas.drawText(dateText, height/40, position - (int)(maxDistance/2), paint);
                    /***************/
                }
                //if previous node should be displayed, make it the new head
                if (position > 0) {
                    Timeline.head = prevNode;
                    curHeadSpot = prevPosition - offset;
                    prevNode.draw(offset, prevNode, false);
                    return;
                }
            }
        }
        else {

            //set regular node position
            position = (int) (prevNode.position +
                    (Math.abs(startEpoch - prevNode.startEpoch) / epochMinute) * minuteSize);

            //too close together
            if (Math.abs(position - prevNode.position) < minDistance)
                position = (int) (prevNode.position + minDistance + 1);

            //too far apart
            else if (Math.abs(position - prevNode.position) >= maxDistance) {
                position = (int) (prevNode.position + maxDistance + 1);

                /***date label***/
                paint.setColor(white);
                canvas.drawRect(0, position - (int)(maxDistance*.65), width,
                        position - (int)(maxDistance*.45), paint);

                dateText = EventText.getDayShort(start.getDay()) + " | " +
                        EventText.getMonthShort(start.getMonth()) + " " + start.getDate();

                paint.setColor(textColor);
                paint.setTextSize(dateLabelSize);
                canvas.drawText(dateText, height/40, position - (int)(maxDistance/2), paint);
                /****************/
            }

        }
        if ((nextNode != null && nextNode.position >= 0) || (position >= 0)) {

            //draw current time bar
            /*paint.setColor(curBarColor);
            curBarPosition = position + (int)(((today - startEpoch)/epochMinute)*minuteSize);
            canvas.drawRect(0, curBarPosition, width, curBarPosition + curBarSize, paint);
            */
            //set up tail
            int tailTop = position + size - 1;
            int tailBottom = tailTop + (int)((Math.abs(startEpoch - endEpoch)/epochMinute)*minuteSize);
            int tailLeft = width / 2 - tailSize - leftShift;
            int tailRight = width / 2 + tailSize - leftShift;

            //calculate if tail overlaps into next day
            if (nextNode != null) {
                int nextPosition = (int) (position + (Math.abs(nextNode.startEpoch - startEpoch)
                        / epochMinute) * minuteSize);

                //if tail overlaps into next day, make it stop at date label
                if ((Math.abs(nextPosition - position) >= maxDistance) &&
                        tailBottom >= position + maxDistance)
                {
                    tailBottom = (int)(position + (maxDistance / 2));
                }
            }

            //set up start text
            String startText = EventText.convertTime(start.getHours(), start.getMinutes());

            //draw tail for event end time
            paint.setColor(tailColor);
            canvas.drawRect(tailLeft, tailTop, tailRight, tailBottom, paint);

            //draw event
            paint.setColor(eventColor);
            canvas.drawCircle(width / 2 - leftShift, position, size, paint);

            //set text properties
            paint.setColor(textColor);
            paint.setTextSize(textSize);

            //draw title text on right hand side
            //check if text spills off the edge, display two lines if it does
            if (!EventText.splitText(title, priority))
                canvas.drawText(title, titleX, position + titleY, paint);
            else {
                canvas.drawText(EventText.firstHalf(title, priority),
                        titleX, position + titleY, paint);

                canvas.drawText(EventText.secondHalf(title, priority),
                        titleX, position + titleY*4, paint);
            }

            //draw start text on left hand side
            paint.setTextSize(leftTextSize);
            canvas.drawText(startText, startX, position + startY, paint);
        }

        //if this is not the last event, and the event is not off the screen
        if ((position < height*.88) && (nextNode != null))
            nextNode.draw(offset, head, false);

        else { //maybe do something if this is the last node

        }
    }
}

//used for text formatting
class EventText {

    final static String[] monthLong = {"January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November", "December"};

    final static String[] monthShort = {"Jan", "Feb", "Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    final static String[] dayShort = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    //right margin cutoffs for title text
    final static int lowCutoff = 26;
    final static int medCutoff = 19;
    final static int highCutoff = 17;

    //returns the full month name as a string
    public static String getMonthLong(int month) {
            return monthLong[month];
        }

    //returns abbreviated month name as a string
    public static String getMonthShort(int month) { return monthShort[month]; }

    //returns abbreviated day of week as a string
    public static String getDayShort(int day) {
        return dayShort[day];
    }

    //formats the event start text to 12 hour time
    public static String convertTime(int hour, int minute) {
        String twelveHour;
        boolean am = true;

        //convert time from 24 hour to 12 hour
        if (hour > 12) {
            hour -= 12;
            am = false;
        }
        else if (hour == 0)
            hour = 12;
        else if (hour == 12)
            am = false;

        twelveHour = Integer.toString(hour) + ":" + Integer.toString(minute);

        //only display hour for events without minute
        /*
        if (minute == 0)
            twelveHour = Integer.toString(hour);
        */

        //display full start time
        if (minute == 0)
            twelveHour += "0"; //pad with a zero since java only prints one zero, e.g. 12:0

        //add an 'a' or 'p' at the end of time
        if (am)
            twelveHour += "a";
        else
            twelveHour += "p";

        return twelveHour;
    }
    //determines if event title text spills off the edge of screen
    public static boolean splitText(String text, int priority) {

        //since title text size is based on priority the cutoff is different for different events
        switch(priority) {

            case 0:
                if (text.length() > lowCutoff)
                    return true;
                else
                    return false;
            case 1:
                if (text.length() > medCutoff)
                    return true;
                else
                    return false;
            case 2:
                if (text.length() > highCutoff)
                    return true;
                else
                    return false;
            default:
                return false;
        }
    }
    //splits event title text at the edge of screen, returns first half
    public static String firstHalf(String text, int priority) {

        //since title text size is based on priority the cutoff is different for different events
        switch (priority) {
            case 0:
                if (text.charAt(lowCutoff + 1) == ' ')
                    return text.substring(0, lowCutoff);
                else
                    return text.substring(0, lowCutoff);

            case 1:
                if (text.charAt(medCutoff + 1) == ' ')
                    return text.substring(0, medCutoff);
                else
                    return text.substring(0, medCutoff);

            case 2:
                if (text.charAt(highCutoff + 1) == ' ')
                    return text.substring(0, highCutoff);
                else
                    return text.substring(0, highCutoff);

            default:
                return text;
        }
    }
    //splits event title text at the edge of screen, returns second half
    public static String secondHalf(String text, int priority) {

        //since title text size is based on priority the cutoff is different for different events
        switch (priority) {
            case 0: return text.substring(lowCutoff);

            case 1: return text.substring(medCutoff);

            case 2: return text.substring(highCutoff);

            default:
                return text;
        }

    }
}

