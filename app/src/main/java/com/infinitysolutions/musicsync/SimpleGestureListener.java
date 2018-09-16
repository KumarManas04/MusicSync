package com.infinitysolutions.musicsync;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
    private Listener mListener;

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mListener == null)
            return true;
        if (Math.abs(distanceX) != 0 && Math.abs(distanceY) > 60) {
            mListener.onScrollVertical(distanceY);
        }
        return true;
    }

    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }

    interface Listener {
        // upward scroll dy > 0    downward scroll dy < 0
        void onScrollVertical(float dy);
    }
}