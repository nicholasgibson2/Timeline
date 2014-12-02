package cogs198.timeline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Scroller;
import android.widget.Toast;



public class Timeline extends View implements View.OnTouchListener{

    int width, height;

    static Event head = null;

    public static int offset = 0;
    static Canvas canvas;
    static Paint paint = new Paint();
    static Context holyContext;
    int leftShift;
    int timelineColor = Color.parseColor("#cfdfee");
    int backgroundColor = Color.parseColor("#ffffff");
    static double screenDensity;

    static Scroller mScroller;
    boolean first = true;

    static float prevY = 0;

    // CONSTRUCTOR
    public Timeline(Context context) {
        super(context);
        setFocusable(true);

        setBackgroundColor(backgroundColor);

        holyContext = context;

        //used for getting screen dimensions
        WindowManager wm = (WindowManager) holyContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        width = display.getWidth(); //width of screen
        height = display.getHeight(); //height of screen
        leftShift = height/8;

        screenDensity = getResources().getDisplayMetrics().density;

        mScroller = new Scroller(context);
    }
    public void setHead(Event setHead) {
        head = setHead.updateHead(setHead);
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

        paint.setColor(backgroundColor);
        canvas.drawRect(timelineLeft + (height/400), timelineTop, timelineRight -
                (height/400), height, paint);

        setOnTouchListener(this);

        if (head != null) {
            Event.paint = paint;
            Event.canvas = canvas;
            drawEvents();
        }
    }

    void drawEvents() {

        head.draw(offset, head, first);
        first = false;

        //Toast.makeText(holyContext, head.title, Toast.LENGTH_SHORT).show();
        invalidate();
    }

    //makeshift scrolling method, until real scrolling is implemented
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (prevY == 0) {
            prevY = event.getY();
            return true;
        }

        offset -= 1.2*(int) (prevY - event.getY());

        drawEvents();

        prevY = event.getY();

        if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
            prevY = 0;
        }

        return true;
    }

}





