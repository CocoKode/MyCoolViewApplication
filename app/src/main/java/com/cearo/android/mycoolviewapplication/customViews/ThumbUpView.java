package com.cearo.android.mycoolviewapplication.customViews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.cearo.android.mycoolviewapplication.R;

public class ThumbUpView extends View {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private AnimatorSet mTouchAnimator;
    private AnimatorSet mThumbAnimator;
    private AnimatorSet mNumberAnimator;
    private AnimatorSet mReverseNumAnimator;

    private Bitmap mUnselectedThumb;
    private Bitmap mSelectedThumb;
    private Bitmap mShining;

    private int touchRadius;
    private float fadeTouch = 1f;
    private float scale = 1f;
    private int diffNum = 0;
    private float fadeNum = 1f;

    private int thumbRadius;
    private float fadeThumb;

    private boolean isTapped = false;
    private int mNumber = 19;
    private float centerX;
    private float centerY;
    private Rect mTextRect = new Rect();

    private int status = -1;

    private int duration = 400;

    public ThumbUpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        mPaint.setTextSize(20);
        mUnselectedThumb = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_messages_like_unselected);
        mSelectedThumb = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_messages_like_selected);
        mShining = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_messages_like_selected_shining);

        ObjectAnimator animator1 = ObjectAnimator.ofInt(this, "touchRadius", 0, 20);
        animator1.setDuration(duration);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "fadeTouch", 1, 0);
        animator2.setDuration(duration);
        mTouchAnimator = new AnimatorSet();
        mTouchAnimator.playTogether(animator1, animator2);
        mTouchAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                reset();
            }
        });


        // 1.灰拇指变小2.替换为彩拇指放大，并射线状弹出闪光图3.伴随1、2两个过程，还有一个从图片中心扩散的圆
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(this, "scale", 1f, 0.8f, 1.2f, 1f);
        animator3.setDuration(duration);

        ObjectAnimator animator111 = ObjectAnimator.ofInt(this, "thumbRadius", 0, 20);
        animator111.setDuration(duration);
        ObjectAnimator animator222 = ObjectAnimator.ofFloat(this, "fadeThumb", 1, 0);
        animator222.setDuration(duration);
        AnimatorSet thumbCircle = new AnimatorSet();
        thumbCircle.playTogether(animator111, animator222);
        thumbCircle.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                resetThumbEffect();
            }
        });


//        ObjectAnimator animator33 = ObjectAnimator.ofFloat(this, "scaleSelected", 1.5f, 1f);
        mThumbAnimator = new AnimatorSet();
        mThumbAnimator.playTogether(animator3, thumbCircle);


        ObjectAnimator animator4 = ObjectAnimator.ofInt(this, "diffNum", 0, 20);
        animator4.setDuration(duration);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(this, "fadeNum", 1, 0);
        animator5.setDuration(duration);
        mNumberAnimator = new AnimatorSet();
        mNumberAnimator.playTogether(animator4, animator5);

        ObjectAnimator animator6 = ObjectAnimator.ofInt(this, "diffNum", 20, 0);
        animator6.setDuration(duration);
        ObjectAnimator animator7 = ObjectAnimator.ofFloat(this, "fadeNum", 0, 1);
        animator7.setDuration(duration);
        mReverseNumAnimator = new AnimatorSet();
        mReverseNumAnimator.playTogether(animator6, animator7);
    }

    private void resetThumbEffect() {
        thumbRadius = 0;
        fadeThumb = 1f;
    }

    private void reset() {
        touchRadius = 0;
        fadeTouch = 1f;
    }

    private int marginBetweenBitmapAndText = 8;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String numStr = String.valueOf(mNumber);
        String[] nextNum = splitNextNum(mNumber);
        mPaint.getTextBounds(numStr, 0, numStr.length(), mTextRect);
        int textWidth = mTextRect.width();
        int textHeight = mTextRect.height();

        Bitmap bitmap;
        if (status == STATUS_CHECK) {
            bitmap = mSelectedThumb;
        } else {
            bitmap = mUnselectedThumb;
        }

        int bmWidth = bitmap.getWidth();
        int bmHeight = bitmap.getHeight();

        int centerWidth = getWidth() / 2;
        int centerHeight = getHeight() / 2;
        int totalWidth = textWidth + bmWidth + marginBetweenBitmapAndText;
        int totalHeight = Math.max(textHeight, bmHeight);

        // 画点击时放大缩小的动画，canvas放大后绘制好像会模糊失真
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(255);

        canvas.save();
        canvas.scale(scale, scale, centerWidth - totalWidth / 2 + bmWidth / 2, centerHeight - totalHeight / 2 + bmHeight / 2);
        // 或者对单个图片进行matrix变换
//        Matrix newMatrix = new Matrix();
//        newMatrix.preScale(3.5f, 3.5f, mSelectedThumb.getWidth() / 2, mSelectedThumb.getHeight() / 2);
//        canvas.drawBitmap(mSelectedThumb, newMatrix, mPaint);

        if (status == STATUS_CHECK) {
            // 闪光效果图片
            canvas.drawBitmap(mShining,
                    centerWidth - totalWidth / 2 + 1, centerHeight - totalHeight / 2f - 8,
                    mPaint);
        }

        // 大拇指图片
        canvas.drawBitmap(bitmap,
                centerWidth - totalWidth / 2, centerHeight - totalHeight / 2,
                mPaint);
        canvas.restore();

        // 画淡入淡出的数字

        if (status == -1) {
            canvas.drawText(numStr, centerWidth - totalWidth / 2f + bmWidth + marginBetweenBitmapAndText, centerHeight + textHeight / 2 - diffNum, mPaint);

        } else {
            float remainWidth = mPaint.measureText(nextNum[0]);

            // 不变的部分
            canvas.drawText(nextNum[0], centerWidth - totalWidth / 2f + bmWidth + marginBetweenBitmapAndText, centerHeight + textHeight / 2, mPaint);

            // 消失的部分
            mPaint.setAlpha((int) (fadeNum * 255));
            canvas.drawText(nextNum[1], centerWidth - totalWidth / 2f + bmWidth + marginBetweenBitmapAndText + remainWidth, centerHeight + textHeight / 2 - diffNum, mPaint);

            // 显示的部分
            mPaint.setAlpha((int) ((1 - fadeNum) * 255));
            canvas.drawText(nextNum[2], centerWidth - totalWidth / 2f + bmWidth + marginBetweenBitmapAndText + remainWidth, centerHeight + textHeight / 2 + 20 - diffNum, mPaint);

        }

        mPaint.setStyle(Paint.Style.STROKE);
        // 画点击扩散的圆圈
        if (touchRadius > 0) {
            mPaint.setAlpha((int) (fadeTouch * 255));
            canvas.drawCircle(centerX, centerY, touchRadius, mPaint);
        }

        // 画点赞扩散的圆圈
        if (thumbRadius > 0) {
            mPaint.setAlpha((int) (fadeThumb * 255));
            canvas.drawCircle(centerWidth - totalWidth / 2 + bmWidth / 2, centerHeight - totalHeight / 2 + bmHeight / 2, thumbRadius, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            centerX = event.getX();
            centerY = event.getY();
            mTouchAnimator.start();

            setStatus();
            if (status == STATUS_CHECK) {
                start();
            } else if (status == STATUS_CANCEL){
                reverse();
            }

        }
        return super.onTouchEvent(event);
    }


    private void start() {
        mThumbAnimator.start();
        mNumberAnimator.start();
    }

    private void reverse() {
        mThumbAnimator.start();
        mReverseNumAnimator.start();
    }

    public void setTouchRadius(int touchRadius) {
        this.touchRadius = touchRadius;
        invalidate();
    }

    public void setFadeTouch(float fadeTouch) {
        this.fadeTouch = fadeTouch;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTouchAnimator.cancel();
        mThumbAnimator.cancel();
        mNumberAnimator.cancel();
    }

    public void setScale(float scale) {
        this.scale = scale;
        invalidate();
    }

    public void setDiffNum(int diffNum) {
        this.diffNum = diffNum;
        invalidate();
    }

    public void setFadeNum(float fadeNum) {
        this.fadeNum = fadeNum;
        invalidate();
    }

    public static final int STATUS_CHECK = 0;
    public static final int STATUS_CANCEL = 1;

    public void setStatus() {
        status = (status + 1) % 2;
    }

    public void setThumbRadius(int thumbRadius) {
        this.thumbRadius = thumbRadius;
        invalidate();
    }

    public void setFadeThumb(float fadeThumb) {
        this.fadeThumb = fadeThumb;
        invalidate();
    }


    public static String[] splitNextNum(int num) {
        String[] result = new String[3];
        int index = 0;
        int ret = num;
        while (ret % 10 == 9) {
            index++;
            ret /= 10;
        }

        if (index > 0) {
            index++;
        } else {
            index = 1;
        }

        String numStr = String.valueOf(num);
        String nextNumStr = String.valueOf(num + 1);
        if (ret < 9) {
            result[0] = "";
            result[1] = numStr;
            result[2] = nextNumStr;
        } else {
            result[0] = nextNumStr.substring(0, numStr.length() - index);
            result[1] = numStr.substring(numStr.length() - index);
            result[2] = nextNumStr.substring(numStr.length() - index);
        }

        return result;
    }
}
