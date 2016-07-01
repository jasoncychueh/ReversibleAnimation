
package com.foureach.graphics.drawable;

import android.graphics.drawable.Drawable;

/**
 * An animation listener receives notifications from an animation drawable. Notifications indicate
 * animation related events, such as the end or the frame changes of the animation.
 *
 * Created by JasonCYChueh on 2016/6/17.
 */
public interface AnimationListener {
    /**
     * Notifies the start of the animation.
     *
     * @param animation the LazyLoadingAnimationDrawable
     */
    void onAnimationStart(ReversibleAnimationDrawable animation);

    /**
     * Notifies when the next frame is scheduled to be drawn. If the frame is skipped, the previous
     * frame is used.
     *
     * @param animation the LazyLoadingAnimationDrawable
     * @param frame the index of the frame
     * @param skipped true is this frame is skipped, false if not.
     */
    void onNextFrame(ReversibleAnimationDrawable animation, int frame, Drawable drawable,
            boolean skipped);

    /**
     * Notifies the end of the animation. This callback is not invoked for animations with oneshot
     * set to false.
     *
     * @param animation the LazyLoadingAnimationDrawable
     */
    void onAnimationEnd(ReversibleAnimationDrawable animation);

    class AnimationListenerAdapter implements AnimationListener {
        @Override
        public void onAnimationStart(ReversibleAnimationDrawable animation) {

        }

        @Override
        public void onNextFrame(ReversibleAnimationDrawable animation, int frame,
                Drawable drawable, boolean skipped) {

        }

        @Override
        public void onAnimationEnd(ReversibleAnimationDrawable animation) {

        }
    }
}
