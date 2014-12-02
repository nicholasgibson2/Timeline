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
    int priority;
    String title;
    long startEpoch; //used for actual time comparisons
    long endEpoch;
    int position = 0;
    int size; //defined by priority
    boolean repeated = false;
    /**********************************************************************************************/

    /************************************misc setup************************************************/
    //used for getting screen dimensions
    final static WindowManager wm = (WindowManager) Timeline.holyContext.getSystemService(Context.WINDOW_SERVICE);
    final static Display display = wm.getDefaultDisplay();
    final static int width = display.getWidth(); //width of screen
    final static int height = display.getHeight(); //height of screen
    final static int epochMinute = 60000;
    static Canvas canvas;
    static Paint paint;
    static int numEvents = 0; //used for debugging
    final static long today = System.currentTimeMillis();
    static int curBarPosition;
    /**********************************************************************************************/

    /************************************user interface********************************************/
    //spacing
    final static double headBuffer = height / 6.5; //buffer for first event from top of screen
    final static int minDistance = height / 11; //minimum event spacing
    final static double maxDistance = height*.4; //maximum event spacing
    final static double minuteSize = height / 660; //event spacing per minute
    final static int leftShift = height / 10; //x position of timeline

    //event sizes
    final static int lowSize = height / 44; //low priority event size
    final static int medSize = height / 24; //medium priority event size
    final static int highSize = height / 18; //high priority event size
    final static int tailSize = height / 133; //width of timeline, size of gap boxes

    //positioning for event titles
    final static int titleX = width / 2 - height / 40;
    final static int titleY = height / 61;
    final static int startX = (int)(height / 80);
    final static int startY = titleY;

    //colors
    int tailColor = Color.parseColor("#b0c6dd"); //in case we want different color for tail
    int eventColor = Color.parseColor("#b0c6dd"); //will be set to lowColor, medColor, or highColor
    //final static int lowColor;
    //final static int medColor;
    //final static int highColor;
    final static int textColor = Color.parseColor("#000000");
    final static int topColor = Color.parseColor("#ffffff");
    final static int curBarColor = Color.parseColor("#FF0000"); //color of current time bar

    //text sizes
    final static int topTextSize = (int)(35 * Timeline.screenDensity);
    final static int leftTextSize = (int)(18 * Timeline.screenDensity);
    int textSize = (int)(20 * Timeline.screenDensity);

    final static int curBarSize = (int)(2*Timeline.screenDensity); //size of current time bar
    /**********************************************************************************************/


    /************************************node data*************************************************/
    static int curHeadSpot = 0; //position of current head node

    //low med high nodes not set up yet, will be used for easy view by priority
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
                textSize = (int)(18 * Timeline.screenDensity);
                //eventColor = lowColor;
                break;
            case 1:
                size = medSize;
                textSize = (int)(25 * Timeline.screenDensity);
                //eventColor = medColor;
                break;
            case 2:
                size = highSize;
                textSize = (int)(28 * Timeline.screenDensity);
                //eventColor = highColor;
                break;
            default:
                size = lowSize;
                //eventColor = lowColor;
                break;
        }
        tailColor = eventColor;
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

            if (oldPrev != null)
                oldPrev.nextNode = prevNode;

            if (this == head)
                return prevNode;
            else
                return head;
        }
    }

    //initially called from head, called recursively until events are off the display
    void draw(int offset, Event head, boolean first) {

        String dateText;

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
                    prevPosition = (int) (prevPosition - minDistance - 1);

                //too far apart
                else if (Math.abs(prevPosition - position) >= maxDistance) {
                    prevPosition = (int) (position - maxDistance - 1);

                    //date label
                    /*paint.setColor(textColor);
                    canvas.drawRect(0, position - (int)(maxDistance*.66), width,
                            position - (int)(maxDistance*.4425), paint);
*/
                    paint.setColor(topColor);
                    canvas.drawRect(0, position - (int)(maxDistance*.65), width,
                            position - (int)(maxDistance*.45), paint);

                    dateText = EventDate.getDayShort(start.getDay()) + ", " +
                            EventDate.getMonthLong(start.getMonth()) + " " + start.getDate();

                    paint.setColor(textColor);
                    paint.setTextSize(topTextSize);
                    canvas.drawText(dateText, height/40, position - (int)(maxDistance/2), paint);
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
                    (Math.abs(startEpoch - prevNode.startEpoch) / epochMinute) * minuteSize);

            //too close together
            if (Math.abs(position - prevNode.position) < minDistance)
                position = (int) (prevNode.position + minDistance + 1);

            //too far apart
            else if (Math.abs(position - prevNode.position) >= maxDistance) {
                position = (int) (prevNode.position + maxDistance + 1);

                //date label
                paint.setColor(textColor);
               /* canvas.drawRect(0, position - (int)(maxDistance*.66), width,
                        position - (int)(maxDistance*.4425), paint);
*/
                paint.setColor(topColor);
                canvas.drawRect(0, position - (int)(maxDistance*.65), width,
                        position - (int)(maxDistance*.45), paint);

                dateText = EventDate.getDayShort(start.getDay()) + ", " +
                        EventDate.getMonthLong(start.getMonth()) + " " + start.getDate();

                paint.setColor(textColor);
                paint.setTextSize(topTextSize);
                canvas.drawText(dateText, height/40, position - (int)(maxDistance/2), paint);
            }

        }
        if ((nextNode != null && nextNode.position >= 0) || (position >= 0)) {

            //draw current time bar
            /*paint.setColor(curBarColor);
            curBarPosition = position + (int)(((today - startEpoch)/epochMinute)*minuteSize);
            canvas.drawRect(0, curBarPosition, width, curBarPosition + curBarSize, paint);
            */
            //set up tail
            int tailTop = position + size;
            int tailBottom = tailTop + (int)((Math.abs(startEpoch - endEpoch)/epochMinute)*minuteSize);
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
            paint.setTextSize(leftTextSize);
            canvas.drawText(startText, startX, position + startY, paint);
        }



        //if this is not the last event, and the event is not off the screen
        if ((position < height*.88) && (nextNode != null))
            nextNode.draw(offset, head, false);
        else {

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

