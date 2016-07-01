
package com.foureach.graphics.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Xml;

import com.foureach.reversibleanimationdrawable.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by JasonCYChueh on 2016/3/8.
 */
public class LazyLoadingAnimationDrawable extends ReversibleAnimationDrawable {
    private static final String TAG = "LazyLoadingAnimDrawable";

    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    private static final int CACHE_SIZE = 5;

    private AnimationState mAnimationState;

    private LinkedList<FrameCache> mCachedFrames = new LinkedList<>();

    /**
     * The current mId, ranging from 0 to {@link #mAnimationState#getChildCount() - 1}
     */
    private int mCurFrame = 0;

    /**
     * Whether the mDrawable has an animation callback posted.
     */
    private boolean mRunning;

    /**
     * Whether the mDrawable should animate when visible.
     */
    private boolean mAnimating = false;

    private boolean mReverse = false;

    private FrameCache mCurrent;

    private AnimationListener mAnimationListener;

    public LazyLoadingAnimationDrawable(Resources res) {
        mAnimationState = new AnimationState(res, null);
    }

    public LazyLoadingAnimationDrawable(Resources res, Resources.Theme theme) {
        mAnimationState = new AnimationState(res, theme);
    }

    public static LazyLoadingAnimationDrawable loadFromResource(Resources res, int id)
            throws Resources.NotFoundException {
        try {
            return createFromXml(res, res.getXml(id));
        } catch (Exception e) {
            throw new Resources.NotFoundException(e.getMessage());
        }
    }

    /**
     * Create a mDrawable from an XML document. For more information on how to create resources in
     * XML, see <a href="{@docRoot}guide/topics/resources/mDrawable-resource.html">Drawable
     * Resources</a>.
     */
    public static LazyLoadingAnimationDrawable createFromXml(Resources r, XmlPullParser parser)
            throws XmlPullParserException, IOException {
        return createFromXml(r, parser, null);
    }

    /**
     * Create a drawable from an XML document using an optional {@link Resources.Theme}. For more
     * information on how to create resources in XML, see <a href=
     * "{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.
     */
    public static LazyLoadingAnimationDrawable createFromXml(Resources r, XmlPullParser parser,
            Resources.Theme theme) throws XmlPullParserException, IOException {
        AttributeSet attrs = Xml.asAttributeSet(parser);

        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG &&
                type != XmlPullParser.END_DOCUMENT) {
            // Empty loop
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        String name = parser.getName();
        if (name.equals("animation-list")) {
            LazyLoadingAnimationDrawable drawable = new LazyLoadingAnimationDrawable(r, theme);
            drawable.inflate(r, parser, attrs, theme);
            return drawable;
        } else {
            throw new RuntimeException("Unknown initial tag: " + parser.getName());
        }
    }

    /**
     * Sets whether this AnimationDrawable is visible. When the drawable becomes invisible, it will
     * pause its animation. A subsequent change to visible with <code>restart</code> set to true
     * will restart the animation from the first frame. If <code>restart</code> is false, the
     * animation will resume from the most recent frame.
     *
     * @param visible true if visible, false otherwise
     * @param restart when visible, true to force the animation to restart from the first frame
     * @return true if the new visibility is different than its previous state
     */
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (visible) {
            if (restart || changed) {
                boolean startFromZero = restart || !mRunning ||
                        mCurFrame >= mAnimationState.getSize();
                setFrame(startFromZero ? 0 : mCurFrame, true, mAnimating);
            }
        } else {
            unscheduleSelf(this);
        }
        return changed;
    }

    /**
     * Starts the animation, looping if necessary. This method has no effect if the animation is
     * running. <strong>Note:</strong> Do not call this in the {@link android.app.Activity#onCreate}
     * method of your activity, because the {@link LazyLoadingAnimationDrawable} is not yet fully
     * attached to the window. If you want to play the animation immediately without requiring
     * interaction, then you might want to call it from the
     * {@link android.app.Activity#onWindowFocusChanged} method in your activity, which will get
     * called when Android brings your window into focus.
     *
     * @see #isRunning()
     * @see #stop()
     */
    @Override
    public void start() {
        mAnimating = true;

        if (!isRunning() && isVisible()) {
            if (mAnimationListener != null) {
                mAnimationListener.onAnimationStart(this);
            }
            // Start from current frame.
            setFrame(mCurFrame, false, mAnimationState.getSize() > 1 || !mAnimationState.mOneShot);
        }
    }

    /**
     * Stops the animation. This method has no effect if the animation is not running.
     *
     * @see #isRunning()
     * @see #start()
     */
    @Override
    public void stop() {
        mAnimating = false;

        if (isRunning()) {
            unscheduleSelf(this);
            mCachedFrames.clear();
        }
    }

    /**
     * Indicates whether the animation is currently running or not.
     *
     * @return true if the animation is running, false otherwise
     */
    @Override
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * This method exists for implementation purpose only and should not be called directly. Invoke
     * {@link #start()} instead.
     *
     * @see #start()
     */
    @Override
    public void run() {
        nextFrame(false);
    }

    @Override
    public void unscheduleSelf(Runnable what) {
        mRunning = false;
        super.unscheduleSelf(what);
    }

    @Override
    public void draw(Canvas canvas) {
        if (isVisible()) {
            mCurrent.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public void setDither(boolean dither) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs,
            Resources.Theme theme) throws XmlPullParserException, IOException {
        inflateChildElements(r, parser, attrs);

        TypedArray a = r.obtainAttributes(attrs, R.styleable.LazyLoadingAnimationDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        int type;
        final int innerDepth = parser.getDepth() + 1;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }

            final TypedArray a = r.obtainAttributes(attrs,
                    R.styleable.LazyLoadingAnimationDrawable);

            final int duration = a.getInt(R.styleable.LazyLoadingAnimationDrawable_android_duration,
                    -1);
            if (duration < 0) {
                throw new XmlPullParserException(parser.getPositionDescription()
                        + ": <item> tag requires a 'duration' attribute");
            }

            int id = a.getResourceId(R.styleable.LazyLoadingAnimationDrawable_android_drawable, -1);
            mAnimationState.addFrame(id, duration);
            a.recycle();
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        mAnimationState.mOneShot = a
                .getBoolean(R.styleable.LazyLoadingAnimationDrawable_android_oneshot, false);
        boolean visible = a.getBoolean(R.styleable.LazyLoadingAnimationDrawable_android_visible,
                true);
        setVisible(visible, true);
    }

    public void addFrame(@DrawableRes int resid, int duration) {
        mAnimationState.addFrame(resid, duration);
        if (!mRunning) {
            setFrame(0, true, false);
        }
    }

    private void nextFrame(boolean unschedule) {
        int nextFrame = !mReverse ? mCurFrame + 1 : mCurFrame - 1;
        final int numFrames = mAnimationState.getSize();
        final boolean isLastFrame = mAnimationState.mOneShot
                && (!mReverse ? nextFrame >= numFrames - 1 : nextFrame <= 0);

        // Loop if necessary. One-shot animations should never hit this case.
        if (!mAnimationState.mOneShot) {
            if (nextFrame >= numFrames) {
                nextFrame = 0;
            } else if (nextFrame <= 0) {
                nextFrame = numFrames - 1;
            }
        }
        setFrame(nextFrame, unschedule, !isLastFrame);
        // Log.d(TAG, "nextFrame: nextFrame=" + nextFrame + ", mCurrent.mIndex=" + mCurrent.mIndex);
        if (mAnimationListener != null && isLastFrame) {
            mAnimationListener.onAnimationEnd(this);
        }
    }

    private void setFrame(int index, boolean unschedule, boolean animate) {
        if (index < 0 || index >= mAnimationState.getSize()) {
            return;
        }
        mAnimating = animate;
        selectFrame(index, animate);
        if (unschedule || animate) {
            unscheduleSelf(this);
        }
        if (animate) {
            // Unscheduling may have clobbered these values; restore them
            mCurFrame = index;
            mRunning = true;
            scheduleSelf(this, SystemClock.uptimeMillis() + mAnimationState.getDuration(index));
        }
        if (mAnimationListener != null && mRunning) {
            mAnimationListener.onNextFrame(this, mCurrent.mIndex, mCurrent.mDrawable,
                    mCurrent.mSkipped);
        }
        if (!animate) {
            mCachedFrames.clear();
            mRunning = false;
        }
    }

    @Override
    public boolean selectDrawable(int idx) {
        if (idx == mCurFrame) {
            return false;
        }
        selectFrame(idx, false);
        return true;
    }

    @Override
    public int getMinimumWidth() {
        return getIntrinsicWidth();
    }

    @Override
    public int getMinimumHeight() {
        return getIntrinsicHeight();
    }

    private void selectFrame(int frame, boolean animate) {
        mCurFrame = frame;
        if (animate) {
            final int numFrames = mAnimationState.getSize();
            for (FrameCache f; !mCachedFrames.isEmpty();) {
                f = mCachedFrames.poll();
                if (f.mIndex == frame) {
                    if (f.mDrawable != null) {
                        mCurrent = f;
                        mCurrent.mSkipped = false;
                    } else if (f.mIndex != numFrames - 1) {
                        f.mDrawable = mCurrent.mDrawable;
                        mCurrent = f;
                        mCurrent.mSkipped = true;
                    }
                    break;
                }
            }

            int d = !mReverse ? 1 : -1;

            int nextFrame = (mCachedFrames.isEmpty() ? frame : mCachedFrames.getLast().mIndex) + d;
            for (; mCachedFrames.size() < CACHE_SIZE; nextFrame += d) {
                if (!mReverse && nextFrame >= numFrames) {
                    nextFrame = 0;
                } else if (mReverse && nextFrame <= 0) {
                    nextFrame = numFrames - 1;
                }
                FrameCache f = new FrameCache(mAnimationState, mAnimationState.getFrame(nextFrame));
                EXECUTOR.execute(f);
                mCachedFrames.add(f);
            }
        }
        if (mCurrent == null || mCurrent.mIndex != frame) {
            mCurrent = new FrameCache(mAnimationState, mAnimationState.getFrame(frame));
            mCurrent.loadFrame();
        }
        mCurrent.mDrawable.setBounds(getBounds());
        invalidateSelf();
    }

    @Override
    public int getCurrentFrameIndex() {
        return mCurFrame;
    }

    @Override
    public Drawable getFrame(int index) {
        Frame frame = mAnimationState.getFrame(index);
        return mAnimationState.loadFrame(frame);
    }

    @Override
    public Drawable getCurrent() {
        return mCurrent.mDrawable;
    }

    /**
     * @return The duration in milliseconds of the frame at the specified index
     */
    public int getDuration(int i) {
        return mAnimationState.getDuration(i);
    }

    /**
     * @return True of the animation will play once, false otherwise
     */
    public boolean isOneShot() {
        return mAnimationState.mOneShot;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mCurrent.mDrawable.setBounds(left, top, right, bottom);
    }

    /**
     * Sets whether the animation should play once or repeat.
     *
     * @param oneShot Pass true if the animation should only play once
     */
    @Override
    public void setOneShot(boolean oneShot) {
        mAnimationState.mOneShot = oneShot;
    }

    @Override
    public boolean isReverse() {
        return mReverse;
    }

    @Override
    public void setReverse(boolean reverse) {
        mReverse = reverse;
    }

    /**
     * @return The number of frames in the animation
     */
    @Override
    public int getNumberOfFrames() {
        return mAnimationState.getSize();
    }

    /**
     * Get the intrinsic width of the first frame.
     *
     * @return the intrinsic width
     */
    @Override
    public int getIntrinsicWidth() {
        return mAnimationState.mWidth;
    }

    /**
     * Get the intrinsic height of the first frame.
     *
     * @return the intrinsic height
     */
    @Override
    public int getIntrinsicHeight() {
        return mAnimationState.mHeight;
    }

    @Override
    public void setAutoMirrored(boolean mirrored) {
        mAnimationState.mAutoMirrored = mirrored;
    }

    @Override
    public boolean isAutoMirrored() {
        return mAnimationState.mAutoMirrored;
    }

    @Override
    public Drawable.ConstantState getConstantState() {
        return mAnimationState;
    }

    @Override
    public void setAnimationListener(AnimationListener listener) {
        mAnimationListener = listener;
    }

    private static class AnimationState extends Drawable.ConstantState {
        Resources mRes;
        Resources.Theme mTheme;

        List<Frame> mFrames = new ArrayList<>();

        boolean mOneShot = false;

        boolean mAutoMirrored = false;

        int mWidth = -1;
        int mHeight = -1;

        AnimationState(Resources res, Resources.Theme theme) {
            mRes = res;
            mTheme = theme;
        }

        @Override
        public Drawable newDrawable() {
            return new LazyLoadingAnimationDrawable(mRes);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new LazyLoadingAnimationDrawable(res);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        int getDuration(int index) {
            return mFrames.get(index).mDuration;
        }

        void addFrame(@DrawableRes int resid, int dur) {
            Frame f = new Frame(mFrames.size(), resid, dur);
            if (mFrames.size() == 0) {
                Drawable d = loadFrame(f);
                mWidth = d.getIntrinsicWidth();
                mHeight = d.getIntrinsicHeight();
            }
            mFrames.add(f);
        }

        Frame getFrame(int index) {
            return mFrames.get(index);
        }

        int getSize() {
            return mFrames.size();
        }

        Drawable loadFrame(Frame frame) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return mRes.getDrawable(frame.mResId, mTheme);
            } else {
                return mRes.getDrawable(frame.mResId);
            }
        }
    }

    private static class Frame {
        int mDuration;
        int mIndex;
        int mResId;

        Frame(int index, @DrawableRes int resid, int duration) {
            mIndex = index;
            mResId = resid;
            mDuration = duration;
        }
    }

    private static class FrameCache extends Frame implements Runnable {
        AnimationState mAnimationState;
        long mTime;
        Drawable mDrawable;
        boolean mSkipped = false;

        FrameCache(AnimationState animationState, Frame frame) {
            super(frame.mIndex, frame.mResId, frame.mDuration);
            mAnimationState = animationState;
        }

        FrameCache(AnimationState animationState, int index, @DrawableRes int resid, int duration) {
            super(index, resid, duration);
            mAnimationState = animationState;
            mIndex = index;
            mResId = resid;
            mDuration = duration;
        }

        void loadFrame() {
            run();
        }

        @Override
        public void run() {
            try {
                mDrawable = mAnimationState.loadFrame(this);
            } catch (Resources.NotFoundException e) {
                // ignored
            }
        }

        void draw(Canvas canvas) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mDrawable.setAutoMirrored(mAnimationState.mAutoMirrored);
            }
            mDrawable.draw(canvas);
        }
    }
}
