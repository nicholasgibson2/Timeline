package cogs198.timeline;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import cogs198.timeline.Timeline;

public class TimelineScrollView extends ScrollView {


    private OnScrollViewListener mOnScrollViewListener;

    public TimelineScrollView(Context context) {
        super(context);
    }

    public TimelineScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimelineScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public interface OnScrollViewListener {
        void onScrollChanged( TimelineScrollView v, int l, int t, int oldl, int oldt );
    }


    public void setOnScrollViewListener(OnScrollViewListener l) {
        this.mOnScrollViewListener = l;
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mOnScrollViewListener.onScrollChanged( this, l, t, oldl, oldt );
        super.onScrollChanged( l, t, oldl, oldt );
    }



}