package com.cearo.android.mycoolviewapplication.CustomViews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.cearo.android.mycoolviewapplication.R;

public class FlipView extends View {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mBitmap;
    private Camera mCamera;
    private AnimatorSet mAnimatorSet;

    private int degreeRight = 0;
    private int degreeLeft = 0;
    private int degreeZ = 0;

    public FlipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        mPaint.setTextSize(50);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.filpboard, null);
        mCamera = new Camera();

        ObjectAnimator animator1 = ObjectAnimator.ofInt(this, "degreeRight", 0, 45);
        animator1.setDuration(2000);

        ObjectAnimator animator2 = ObjectAnimator.ofInt(this, "degreeZ", 0, -270);
        animator2.setDuration(2000);

        ObjectAnimator animator3 = ObjectAnimator.ofInt(this, "degreeLeft", 0, -45);
        animator3.setDuration(2000);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(animator1, animator2, animator3);
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                reset();
                mAnimatorSet.start();
            }
        });
        mAnimatorSet.start();
    }


    public void setDegreeRight(int degreeRight) {
        this.degreeRight = degreeRight;
        invalidate();
    }

    public void setDegreeZ(int degreeZ) {
        this.degreeZ = degreeZ;
        invalidate();
    }

    public void setDegreeLeft(int degreeLeft) {
        this.degreeLeft = degreeLeft;
        invalidate();
    }

    public void reset() {
        degreeRight = 0;
        degreeZ = 0;
        degreeLeft = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int bmWidth = mBitmap.getWidth();
        int bmHeight = mBitmap.getHeight();

        // 1.画左半部分
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        mCamera.save();
        canvas.rotate(degreeZ);
        canvas.clipRect(- getWidth() / 2, -getHeight() / 2,
                0, getHeight() / 2);
        mCamera.rotateY(-degreeLeft);
        mCamera.applyToCanvas(canvas);
        canvas.rotate(-degreeZ);
        mCamera.restore();
        canvas.translate(- getWidth() / 2, - getHeight() / 2);
        canvas.drawBitmap(mBitmap, (getWidth() - bmWidth) / 2, (getHeight() - bmHeight) / 2, mPaint);
        canvas.restore();


        // 2.右半部分
        // 1.画左半部分
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        mCamera.save();
        canvas.rotate(degreeZ);
        canvas.clipRect(0, -getHeight() / 2,
                getWidth() / 2, getHeight() / 2);
        mCamera.rotateY(-degreeRight);
        mCamera.applyToCanvas(canvas);
        canvas.rotate(-degreeZ);
        mCamera.restore();
        canvas.translate(- getWidth() / 2, - getHeight() / 2);
        canvas.drawBitmap(mBitmap, (getWidth() - bmWidth) / 2, (getHeight() - bmHeight) / 2, mPaint);
        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAnimatorSet.cancel();
    }
}
