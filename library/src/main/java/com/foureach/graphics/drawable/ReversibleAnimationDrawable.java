
package com.foureach.graphics.drawable;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by JasonCYChueh on 2016/3/14.
 */
public class ReversibleAnimationDrawable extends AnimationDrawable {
    private static final String TAG = "ReverseDrawable";

    private static Method sGetCurrentIndex;
    private static Method sSetFrame;

    private static Field smAnimating;
    private static Field smRunning;

    private AnimationListener mAnimationListener;

    static {
        try {
            sGetCurrentIndex = DrawableContainer.class.getDeclaredMethod("getCurrentIndex");
            sGetCurrentIndex.setAccessible(true);

            sSetFrame = AnimationDrawable.class.getDeclaredMethod("setFrame", int.class,
                    boolean.class, boolean.class);
            sSetFrame.setAccessible(true);

            smAnimating = AnimationDrawable.class.getDeclaredField("mAnimating");
            smAnimating.setAccessible(true);

            smRunning = AnimationDrawable.class.getDeclaredField("mRunning");
            smRunning.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private boolean mReverse = false;

    ReversibleAnimationDrawable() {

    }

    public ReversibleAnimationDrawable(AnimationDrawable drawable) {
        final int numOfFrames = drawable.getNumberOfFrames();
        for (int i = 0; i < numOfFrames; i++) {
            Drawable d = drawable.getFrame(i);
            int duration = drawable.getDuration(i);
            addFrame(d, duration);
        }
        setOneShot(drawable.isOneShot());
        setVisible(drawable.isVisible(), false);
    }

    private void nextFrame(boolean unschedule) {
        int currentFrame = getCurrentIndexFromSuper();
        int nextFrame = currentFrame + (mReverse ? -1 : 1);
        final int numFrames = getNumberOfFrames();
        final boolean isLastFrame = isOneShot()
                && (!mReverse ? nextFrame >= numFrames - 1 : nextFrame <= 0);

        // Loop if necessary. One-shot animations should never hit this case.
        if (!isOneShot()) {
            if (nextFrame >= numFrames) {
                nextFrame = 0;
            } else if (nextFrame < 0) {
                nextFrame = numFrames - 1;
            }
        }
        setFrame(nextFrame, unschedule, !isLastFrame);
        if (mAnimationListener != null && isLastFrame) {
            mAnimationListener.onAnimationEnd(this);
        }
    }

    private void setFrame(int frame, boolean unschedule, boolean animate) {
        try {
            sSetFrame.invoke(this, frame, unschedule, animate);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        if (mAnimationListener != null && isRunning()) {
            mAnimationListener.onNextFrame(this, frame, getCurrent(), false);
        }
        if (!animate) {
            try {
                smRunning.set(this, false);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public void setAnimationListener(AnimationListener listener) {
        mAnimationListener = listener;
    }

    public boolean isReverse() {
        return mReverse;
    }

    public void setReverse(boolean reverse) {
        mReverse = reverse;
    }

    public int getCurrentFrameIndex() {
        return getCurrentIndexFromSuper();
    }

    private int getCurrentIndexFromSuper() {
        try {
            return (int) sGetCurrentIndex.invoke(this);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void start() {
        try {
            smAnimating.set(this, true);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        if (!isRunning() && isVisible()) {
            if (mAnimationListener != null) {
                mAnimationListener.onAnimationStart(this);
            }
            // Start from current frame.
            setFrame(getCurrentIndexFromSuper(), false, getNumberOfFrames() > 1 || !isOneShot());
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            super.stop();
        }
    }

    @Override
    public void run() {
        nextFrame(false);
    }
}
