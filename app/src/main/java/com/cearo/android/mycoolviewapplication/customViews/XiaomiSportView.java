package com.cearo.android.mycoolviewapplication.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class XiaomiSportView extends View {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public XiaomiSportView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        mPaint.setTextSize(50);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mPaint.setColor(Color.YELLOW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int restoreCount = canvas.save();
//        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(90);
        canvas.translate(0, -getWidth());
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
//        canvas.drawText("SPORTS", getWidth() / 2, getHeight() / 2, mPaint);
        canvas.restoreToCount(restoreCount);
    }
}
