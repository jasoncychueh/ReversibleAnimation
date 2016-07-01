
package com.foureach.reversibleanimationdrawable.example;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.foureach.graphics.drawable.AnimationListener;
import com.foureach.graphics.drawable.LazyLoadingAnimationDrawable;
import com.foureach.graphics.drawable.ReversibleAnimationDrawable;

public class MainActivity extends Activity {
    private static final String TAG = "TestActivity";

    private ImageView mImage1, mImage2;

    private AnimationListener mAnimationListener = new AnimationListener.AnimationListenerAdapter() {
        @Override
        public void onAnimationStart(ReversibleAnimationDrawable animation) {
            Log.d(TAG, animation.getClass().getSimpleName() + ".onAnimationStart: ");
        }

        @Override
        public void onNextFrame(ReversibleAnimationDrawable animation, int frame, Drawable drawable,
                boolean skipped) {
            Log.d(TAG, animation.getClass().getSimpleName() + ".onNextFrame: " + frame);
            if (frame == 8) {
                animation.stop();
            }
        }

        @Override
        public void onAnimationEnd(ReversibleAnimationDrawable animation) {
            Log.d(TAG, animation.getClass().getSimpleName() + ".onAnimationEnd");
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ReversibleAnimationDrawable d = (ReversibleAnimationDrawable) ((ImageView) view)
                    .getDrawable();
            Log.d(TAG, "onClick: animDrawable.isRunning()=" + d.isRunning());

            if (d.isRunning()) {
                d.stop();
            } else {
                if (d.getCurrentFrameIndex() == d.getNumberOfFrames() - 1) {
                    d.selectDrawable(0);
                }
                Log.d(TAG, "onClick: animDrawable.getCurrentFrameIndex()="
                        + d.getCurrentFrameIndex());
                d.stop();
                d.start();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImage1 = (ImageView) findViewById(R.id.image1);
        mImage2 = (ImageView) findViewById(R.id.image2);

        AnimationDrawable d = (AnimationDrawable) getResources().getDrawable(R.drawable.homer);
        final ReversibleAnimationDrawable anim1 = new ReversibleAnimationDrawable(d);
        anim1.setOneShot(true);
        anim1.setAnimationListener(mAnimationListener);
        mImage1.setImageDrawable(anim1);

        mImage1.setOnClickListener(mOnClickListener);

        final LazyLoadingAnimationDrawable anim2 = LazyLoadingAnimationDrawable
                .loadFromResource(getResources(), R.drawable.homer);
        anim2.setOneShot(true);
        anim2.setAnimationListener(mAnimationListener);
        mImage2.setImageDrawable(anim2);

        mImage2.setOnClickListener(mOnClickListener);
    }
}
