package com.cearo.android.mycoolviewapplication.CustomViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import androidx.annotation.Nullable;

import com.cearo.android.mycoolviewapplication.Utils;

import java.util.Locale;

public class HealthyRuler extends View {

    // 尺子的宽高(这里的宽实际上是绘制的可视区域的宽度，并不是ruler的真实宽度)
    int mRulerWidth;
    int mRulerHeight;
    Rect mContentRect = new Rect();

    // 尺子的最大值和最小值
    int mMinValue = 0;
    int mMaxValue = 10;

    // 长指针个数
    int mLongPointerCount;
    // 短指针个数
    int mShortPointerCount;
    // 长指针长度
    float mLongPointerLength;
    // 短指针长度
    float mShortPointerLength;

    // 指示器长度
    float mIndicatorLength;

    // 指针间的间隔
    float mPointerInterval;
    // 长指针末端和文字间的间隔
    float mPointerAndTextInterval;

    // 滑过的距离，也是绘制时的起始位置
    float mScrollOffset;

    Rect mTextRect = new Rect();

    boolean isBeingDragged = false;
    boolean isBeingFling = false;

    private OverScroller scroller;
    private VelocityTracker velocityTracker;
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;

    // 背景色
    private static final int BACKGROUND_COLOR = Color.rgb(244, 248, 243);

    private Paint mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public HealthyRuler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mBackgroundPaint.setColor(BACKGROUND_COLOR);

        mPointerPaint.setColor(Color.BLACK);
        mPointerPaint.setStyle(Paint.Style.FILL);
        mPointerPaint.setStrokeWidth(Utils.dpToPixel(1));

        mTextPaint.setTextSize(15);
        mTextPaint.setStyle(Paint.Style.FILL);

        mIndicatorPaint.setColor(Color.GREEN);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setStrokeCap(Paint.Cap.ROUND);
        mIndicatorPaint.setStrokeWidth(Utils.dpToPixel(8));

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity();

        scroller = new OverScroller(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    private void init(){
        mRulerWidth = getWidth();
        mRulerHeight = (int) Utils.dpToPixel(100);

        mLongPointerCount = mMaxValue - mMinValue + 1;
        mShortPointerCount = 9;

        mLongPointerLength = Utils.dpToPixel(60);
        mShortPointerLength = Utils.dpToPixel(45);
        mIndicatorLength = Utils.dpToPixel(50);

        mPointerInterval = Utils.dpToPixel(16);
        mPointerAndTextInterval = Utils.dpToPixel(3);

        mContentRect.top = (getHeight() - mRulerHeight) / 2;
        mContentRect.bottom = (getHeight() + mRulerHeight) / 2;
        mContentRect.left = 0;
        mContentRect.right = mRulerWidth;

        mScrollOffset = mContentRect.centerX();

        String minValueString = String.format(Locale.CHINA, "%.1f", (float) mMinValue);
        mTextPaint.getTextBounds(minValueString, 0, minValueString.length(), mTextRect);

        totalLength = mLongPointerCount * mShortPointerCount * mPointerInterval + mPointerInterval;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 1.画背景
        canvas.drawRect(mContentRect, mBackgroundPaint);
        // 2.画刻度
        drawPointersAndText(canvas);
        // 3.画指示器
        drawIndicator(canvas);
    }

    private void drawPointersAndText(Canvas canvas) {
        float startOffset = mContentRect.left + mScrollOffset;
        String text;
        for (int i = mMinValue; i <= mMaxValue; i++) {
            if (mContentRect.contains((int) startOffset, mContentRect.top)) {
                canvas.drawLine(startOffset, mContentRect.top,
                        startOffset, mContentRect.top + mLongPointerLength,
                        mPointerPaint);

                text = String.format(Locale.CHINA, "%.1f", (float)i);
                canvas.drawText(text,
                        startOffset - mTextRect.centerX(),
                        mContentRect.top + mLongPointerLength + mPointerAndTextInterval + mTextRect.height(),
                        mTextPaint);
            }

            if (i == mMaxValue) {
                return;
            }

            for (int j = 0; j < mShortPointerCount; j++) {
                startOffset += mPointerInterval;

                if (mContentRect.contains((int) startOffset, mContentRect.top)) {
                    canvas.drawLine(startOffset, mContentRect.top,
                            startOffset, mContentRect.top + mShortPointerLength,
                            mPointerPaint);
                }
            }

            startOffset += mPointerInterval;
        }
    }

    private void drawIndicator(Canvas canvas) {
        canvas.save();
        canvas.clipRect(mContentRect);
        canvas.drawLine(mContentRect.centerX(), mContentRect.top, mContentRect.centerX(), mContentRect.top + mIndicatorLength, mIndicatorPaint);
        canvas.restore();
    }

    float touchX = 0;
    // 最后一个刻度只有长指针，所以额外加一个间隔
    float totalLength;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                initOrResetVelocityTracker();
                isBeingDragged = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float currX = event.getX();
                float dis = touchX - currX;
                touchX = currX;
                mScrollOffset -= dis;
                mScrollOffset = Math.min(mScrollOffset, mContentRect.centerX());
                mScrollOffset = Math.max(mScrollOffset, (float) mContentRect.centerX() - totalLength);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isBeingDragged = false;
                // 这一步必须加，否则返回的速度一直是0
                velocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                if (Math.abs(velocityTracker.getXVelocity()) > mMinFlingVelocity) {
                    // 太草了，由于offset向左为负，所以是越来越小的，所以右边界要放在minX的位置上
                    scroller.fling((int) mScrollOffset, 0,
                            (int) velocityTracker.getXVelocity(), 0,
                            (int) (mContentRect.centerX() - totalLength), mContentRect.centerX(),
                            0, 0);
                    postInvalidateOnAnimation();
                } else {
                    moveToNearestPointer();
                }

//                recycleVelocityTracker();
                break;
            default:
                return super.onTouchEvent(event);
        }

        velocityTracker.addMovement(event);
        return true;
    }

    private void initOrResetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    /**
     *  本方法在draw中被调用，且在onDraw之前，invalidate时会被调用
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            mScrollOffset = scroller.getCurrX();
            postInvalidateOnAnimation();
        }
//        else {
//            if (!isBeingDragged && !isBeingFling) {
//                moveToNearestPointer();
//            }
//        }
    }

    /**
     *  移动到最近的刻度上
     *  被调用的时机有两种1.拖动结束后校正2.fling结束后校正
     */
    private void moveToNearestPointer() {
        // 滑过的距离
        float distance = mContentRect.centerX() - mScrollOffset;
        // 经过的刻度数
        int offsetCount = (int) (distance / mPointerInterval);
        // 超过离当前位置最靠左的刻度的距离
        float dis = distance % mPointerInterval;
        // 如果超过了一半的刻度距离
        if (dis > mPointerInterval / 2) {
            ++offsetCount;
        }

        float nearestPointer = mContentRect.centerX() - offsetCount * mPointerInterval;
        float nearestOffset = mScrollOffset - nearestPointer;

        if (mScrollOffset != nearestPointer) {
            scroller.startScroll((int) mScrollOffset, 0, (int) nearestOffset, 0, 500);
            postInvalidateOnAnimation();
        }
    }
}
