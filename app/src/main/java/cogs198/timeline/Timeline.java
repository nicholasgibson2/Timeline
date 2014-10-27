package cogs198.timeline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Toast;


public class Timeline extends View implements View.OnTouchListener{

    static Event[] events;
    public static int offset = -200;
   // private Scroller mScroller;
    Canvas canvas;
    Paint paint;
    Context holyContext;

    // CONSTRUCTOR
    public Timeline(Context context, Event[] setEvents) {
        super(context);
        setFocusable(true);


        setBackgroundColor(Color.parseColor("#c9e5e3"));

        events = setEvents;
        holyContext = context;

    }

    @Override
    protected void onDraw(Canvas setCanvas) {

        canvas = setCanvas;
        paint = new Paint();

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        paint.setColor(Color.parseColor("#E5F3FF"));
        canvas.drawRect(getWidth() / 2 - 6 - 90, 0, getWidth() / 2 + 6 - 90, getHeight(), paint);
        setOnTouchListener(this);
        drawEvents();


    }

    void drawEvents() {


        for (int loop = 0; events[loop] != null; loop++) {

            events[loop].draw(canvas, paint, getWidth(), getHeight(), offset);
        }
        invalidate();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        offset -= (int)event.getY();
        //Toast.makeText(holyContext, "working", Toast.LENGTH_SHORT).show();
        //invalidate();
        drawEvents();


        return false;
    }
}





