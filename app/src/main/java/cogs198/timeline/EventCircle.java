package cogs198.timeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;

public class EventCircle extends SurfaceView
{
    public EventCircle(Context context, int w, int h)
    {
        super(context);
        Canvas grid = new Canvas(Bitmap.createBitmap(h,w, Bitmap.Config.ARGB_8888));
        grid.drawColor(Color.RED);
        Paint paint = new Paint();
        paint.setARGB(255, 150, 50, 10);
        paint.setStyle(Paint.Style.FILL);
        grid.drawCircle(w/2, h/2 , w/2, paint);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setARGB(255, 150, 50, 10);
        canvas.drawCircle(10, 10, 10, paint);

    }
}