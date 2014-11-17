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
    long endEpoch;

    int position = 0;
    int size; //defined by priority
    boolean repeated = false;

    static int numEvents = 0; //used for debugging

    //used for getting screen dimensions
    WindowManager wm = (WindowManager) Timeline.holyContext.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    int width = display.getWidth(); //width of screen
    int height = display.getHeight(); //height of screen

    final int gapSize = height / 35; //size of timeline gap surrounding events
    final double headBuffer = height / 6.5; //buffer for first event from top of screen
    final int minDistance = height / 11; //minimum event spacing
    final double maxDistance = height*.4; //maximum event spacing
    final int hourSize = height / 11; //event spacing per hour
    final int lowSize = height / 44; //low priority event size
    final int medSize = height / 24; //medium priority event size
    final int highSize = height / 18; //high priority event size
    final int tailSize = height / 133; //width of timeline, size of gap boxes
    final int leftShift = height / 10; //distance to shift left
    final int titleX = width / 2 - height / 40;
    final int titleY = height / 61;
    final int startX = (int)(height / 80);
    final int startY = titleY;

    final int epochHour = 3600000;
    static Canvas canvas;
    static Paint paint;

    static int curHeadSpot = 0;

    //colors
    int tailColor = Color.parseColor("#b0c6dd");
    int eventColor = Color.parseColor("#b0c6dd");
    //int eventColor = Color.parseColor("#df857b");
    //int textColor = Color.parseColor("#ffffff");
    int textColor = Color.parseColor("#000000");
    //int topColor = Color.parseColor("#c1d9de");
    int topColor = Color.parseColor("#ffffff");

    int topTextSize = (int)(35 * Timeline.screenDensity);
    int textSize = (int)(20 * Timeline.screenDensity);

    Event prevNode = null; //previous event
    Event nextNode = null; //next event

    //set canvas and paint, called once from head node
    void setCanvas(Canvas setCanvas, Paint setPaint) {
        canvas = setCanvas;
        paint = setPaint;
    }

    //makes the head node/first displayed event the next upcoming event
    //called recursively
    Event updateHead(Event oldHead, long today) {
        if (nextNode == null) //this is the last node
            return oldHead; //don't update head
        else if (startEpoch <= today) //next node start was before today
            return nextNode.updateHead(oldHead, today);
        else
            return this;
    }

    Event(String setTitle, long setStart, long setEnd, int setPriority, Event setPrevNode, Event setNextNode) {
        priority = setPriority;
        start = new Date(setStart);
        end = new Date(setEnd);
        title = setTitle;
        startEpoch = setStart;
        endEpoch = setEnd;

        prevNode = setPrevNode;
        nextNode = setNextNode;

        switch (priority) {
            case 0:
                size = lowSize;
                //eventColor = Color.parseColor("#E5F3FF");
                break;
            case 1:
                size = medSize;
                //eventColor = Color.parseColor("#E5F3FF");
                break;
            case 2:
                size = highSize;
                //eventColor = Color.parseColor("#E5F3FF");
                break;
            default:
                size = highSize;
                //eventColor = Color.parseColor("#E5F3FF");
                break;
        }
        numEvents++;
    }
    //this method is always called from the head node, it will never be called with no events
    public Event addEvent(String setTitle, long setStart, long setEnd, int setPriority, Event head)
    {
        if (nextNode == null && prevNode == null) //only one Node
        {
            if (setStart > startEpoch) {
                nextNode = new Event(setTitle, setStart, setEnd, setPriority, this, null);
                return this;
            }
            else {
                prevNode = new Event(setTitle, setStart, setEnd, setPriority, null, this);
                return prevNode;
            }
        }
        else if (setStart > startEpoch) //new event start > old event start
        {
            if (nextNode != null)
                return nextNode.addEvent(setTitle, setStart, setEnd, setPriority, head);

            else //added event is the latest event
            {
                nextNode = new Event(setTitle, setStart, setEnd, setPriority, this, null);
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
    void draw(int offset, Event head, boolean first) {

        if (first) {
            position = (int) headBuffer + offset;
            curHeadSpot = position - offset;
        }
        else if (this == head) {
            position = curHeadSpot + offset;

            //head is off top of screen
            if (position <= 0 && nextNode != null) {
                Timeline.head = nextNode;
                int nextPosition = (int) (position + (Math.abs(nextNode.startEpoch - startEpoch)
                        / epochHour) * hourSize);

                //too close together
                if (Math.abs(nextPosition - position) < minDistance)
                    nextPosition = (int) (prevNode.position + minDistance + 1);

                //too far apart
                else if (Math.abs(nextPosition - position) >= maxDistance)
                    nextPosition = (int) (prevNode.position + maxDistance + 1);

                curHeadSpot = nextPosition - offset;

                nextNode.draw(offset, nextNode, false);
                return;
            }
            //checks if previous node should be displayed and made the new head
            else if (prevNode != null)
            {
                int prevPosition = (int) (position - (Math.abs(startEpoch - prevNode.startEpoch) /
                        epochHour) * hourSize);

                //too close together
                if (Math.abs(prevPosition - position) < minDistance)
                    prevPosition = (int) (prevPosition - minDistance - 1);

                //too far apart
                else if (Math.abs(prevPosition - position) >= maxDistance) {
                    prevPosition = (int) (position - maxDistance - 1);

                    paint.setColor(textColor);
                    canvas.drawRect(0, position - 70, width, position - 67, paint);
                }

                //if previous node should be displayed, make it the new head
                if (prevPosition > 0) {
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
                    (Math.abs(startEpoch - prevNode.startEpoch) / epochHour) * hourSize);

            //too close together
            if (Math.abs(position - prevNode.position) < minDistance)
                position = (int) (prevNode.position + minDistance + 1);

            //too far apart
            else if (Math.abs(position - prevNode.position) >= maxDistance) {
                position = (int) (prevNode.position + maxDistance + 1);
                paint.setColor(textColor);
                canvas.drawRect(0, position - 50, width, position - 48, paint);
            }

        }
        if (position >= 0) {

            //set up tail
            int tailTop = position + size;
            int tailBottom = tailTop + ((int)(Math.abs(startEpoch - endEpoch)/epochHour))*hourSize;
            int tailLeft = width / 2 - tailSize - leftShift;
            int tailRight = width / 2 + tailSize - leftShift;

            //set up date text
            String startText = EventDate.convertTime(start.getHours(), start.getMinutes());

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

            canvas.drawText(title, titleX, position + titleY, paint);

            //draw date text on left hand side
            canvas.drawText(startText, startX, position + startY, paint);
        }

        //if this is not the last event, and the event is not off the screen
        if ((position < height*.88) && (nextNode != null))
            nextNode.draw(offset, head, false);
        else {
            //draw background color rectangle to make gap in timeline around ends of event
            paint.setColor(topColor);
            canvas.drawRect(0, 0, width, (int) (headBuffer * .65), paint);

            String dateText;

            if (prevNode != null && prevNode.position >= 0) {
                dateText = EventDate.getDayShort(prevNode.start.getDay()) + ", " +
                        EventDate.getMonthLong(prevNode.start.getMonth()) + " " +
                        prevNode.start.getDate();
            } else {
                dateText = EventDate.getDayShort(start.getDay()) + ", " +
                        EventDate.getMonthLong(start.getMonth()) + " " +
                        start.getDate();
            }

            //set text properties
            paint.setColor(textColor);
            paint.setTextSize(topTextSize);
            canvas.drawText(dateText, height/40, height/16, paint);

            canvas.drawRect(0, (int) (headBuffer * .6), width, (int) (headBuffer * .65), paint);
        }
    }
}

//used for date text formatting
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

