package cogs198.timeline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.Display;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Toast;



public class Timeline extends View implements View.OnTouchListener{

    int width, height;

    static Event head = null;

    public static int offset = 0;
   // private Scroller mScroller;
    Canvas canvas;
    Paint paint = new Paint();
    static Context holyContext;
    int leftShift;
    int timelineColor = Color.parseColor("#E5F3FF");

    static Scroller mScroller;
    boolean first = true;

    static float prevY = 0;

    // CONSTRUCTOR
    public Timeline(Context context) {
        super(context);
        setFocusable(true);

        setBackgroundColor(Color.parseColor("#c9e5e3"));

        holyContext = context;

        //used for getting screen dimensions
        WindowManager wm = (WindowManager) holyContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        width = display.getWidth(); //width of screen
        height = display.getHeight(); //height of screen
        leftShift = height/10;

        mScroller = new Scroller(context);
    }
    public void setHead(Event setHead) {
        //head = setHead;
        head = setHead.updateHead(setHead, System.currentTimeMillis());
    }

    @Override
    protected void onDraw(Canvas setCanvas) {

        canvas = setCanvas;
        int timelineWidth = height / 133;
        int timelineLeft = (width / 2) - timelineWidth - leftShift;
        int timelineRight = (width/ 2) + timelineWidth - leftShift;
        int timelineTop = 0;

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        paint.setColor(timelineColor);
        canvas.drawRect(timelineLeft, timelineTop, timelineRight, height, paint);
        setOnTouchListener(this);

        head.setCanvas(canvas, paint);

        //Toast.makeText(holyContext, Integer.toString(prevPosition), Toast.LENGTH_SHORT).show();
        drawEvents();
    }

    void drawEvents() {

        head.draw(offset, head, first);
        first = false;

        invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (prevY == 0) {
            prevY = event.getY();
            return true;
        }

        offset -= 1.2*(int) (prevY - event.getY());

        //Toast.makeText(holyContext, "working", Toast.LENGTH_SHORT);

        drawEvents();

        prevY = event.getY();

        if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
            prevY = 0;
        }

        return true;
    }

}





