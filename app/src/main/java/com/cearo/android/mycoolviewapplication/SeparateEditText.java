package com.cearo.android.mycoolviewapplication;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import java.lang.reflect.Field;

public class SeparateEditText extends AppCompatEditText {
    // 表示一个字符组的范围
    private RectF textGroupRect = new RectF();
    // 表示整个控件的范围
    private RectF contentRect = new RectF();
    // 字符宽度
    private static float TEXT_WIDTH = 0;
    // 字符组的个数
    private int mTextGroupSize;
    // 每个字符组的最大可容纳字符个数
    private int mTextMaxLength;
    // 字符组之间的间隔
    private static final float INTERVAL = dp2px(8);
    // 每个bottomLine上的字符
    private StringBuilder[] mTextGroup;
    // 光标显示时长
    private static final int BLINK = 500;
    // 光标开始显示的时间
    private static long mShowCursor;
    // 当前光标所在位置
    private int currCursorPosition = 0;
    private int selectedBottomLineWidth = 3;
    private static final float BOTTOM_LINE_OFFSET = dp2px(7);
    private volatile boolean mCursorVisible;
    private int mLastTextLength = 0;
    private int mBackground;
    private static final int BACKGROUND_LINE = 0;
    private static final int BACKGROUND_ROUND_RECT = 1;
    private String mSeparator;

    private Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);


    public SeparateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackground(null);

        backgroundPaint.setStyle(Paint.Style.STROKE);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SeparateEditText);
        mTextGroupSize = array.getInteger(R.styleable.SeparateEditText_size, 1);
        mTextMaxLength = array.getInteger(R.styleable.SeparateEditText_textMaxLength, 5);
        int color = array.getColor(R.styleable.SeparateEditText_bottomLineColor, Color.BLACK);
        backgroundPaint.setColor(color);
        mBackground = array.getInteger(R.styleable.SeparateEditText_textBackground, BACKGROUND_LINE);
        mCursorVisible = array.getBoolean(R.styleable.SeparateEditText_cursorVisible, true);

        final String separator = array.getString(R.styleable.SeparateEditText_separator);
        if (!TextUtils.isEmpty(separator)) {
            setSeparator(separator);
        }
        array.recycle();

        cursorPaint.setColor(color);
        cursorPaint.setStrokeWidth(2);

        textPaint.setTextSize(getTextSize());
        textPaint.setTextAlign(Paint.Align.CENTER);

        mTextGroup = new StringBuilder[mTextGroupSize];
        for (int i = 0; i < mTextGroupSize; i++) {
            mTextGroup[i] = new StringBuilder(mTextMaxLength);
        }

        Rect rect = new Rect();
        textPaint.getTextBounds("0", 0, 1, rect);
        TEXT_WIDTH = rect.width();
    }

    /**
     * getwidth, getheight是整个控件的最大空间，包含一切
     * 加了padding后就是光标的空间
     * bottomLine不受padding影响，应该是从最底部算起，加了一个offset
     * text大小比光标略小
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        contentRect.left = 0;
        contentRect.top = 0;
        contentRect.right = getWidth();
        contentRect.bottom = getHeight();

        textGroupRect.left = 0;
        textGroupRect.top = getPaddingTop();
        textGroupRect.right = (getWidth() - getPaddingLeft() - getPaddingRight() - INTERVAL * (mTextGroupSize - 1)) / mTextGroupSize;
        textGroupRect.bottom = getHeight() - getPaddingBottom();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mShowCursor = SystemClock.uptimeMillis();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            disableSelectHandler();
            currCursorPosition = calculateCursorPosition(event.getX(), event.getY());
            if (!isFocused()) {
                requestFocus();
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused) {
            currCursorPosition = -1;
        }
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        // 防止内容左移
        setScrollX(0);
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
    }

    /**
     *  禁用edittext的selectHandler,就是选择文本时显示在头尾的两个小指针
     */
    private void disableSelectHandler() {
        try {
            Class clazz = TextView.class;
            Field field = clazz.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(this);
            Class editorClazz = editor.getClass();
            Field enable = editorClazz.getDeclaredField("mInsertionControllerEnabled");
            enable.setAccessible(true);
            enable.set(editor, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private int calculateCursorPosition(float x, float y) {
        if (contentRect.contains(x, y)) {
            return (int) (x / (textGroupRect.width() + INTERVAL));
        }

        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        float padding = getPaddingLeft();
        canvas.translate(padding, 0);

        for (int i = 0; i < mTextGroupSize; i++) {
            float offsetX = i * (textGroupRect.right + INTERVAL);

            // 画背景
            if (i == currCursorPosition) {
                backgroundPaint.setStrokeWidth(selectedBottomLineWidth);
            } else {
                backgroundPaint.setStrokeWidth(1);
            }

            if (mBackground == BACKGROUND_LINE) {
                canvas.drawLine(offsetX + textGroupRect.left, contentRect.bottom - BOTTOM_LINE_OFFSET,
                        offsetX + textGroupRect.right, contentRect.bottom - BOTTOM_LINE_OFFSET,
                        backgroundPaint);
            } else {
                canvas.drawRoundRect(
                        offsetX + textGroupRect.left, contentRect.top + 3,
                        offsetX + textGroupRect.right, contentRect.bottom - 3,
                        5f, 5f,
                        backgroundPaint);
            }

            // 画文字
            float textWidth = 0f;
            if (mTextGroup[i] != null && !TextUtils.isEmpty(mTextGroup[i].toString())) {
                String text = mTextGroup[i].toString();
                textWidth = textPaint.measureText(text);
                canvas.drawText(text, offsetX + textGroupRect.centerX(), textGroupRect.bottom - 3, textPaint);
            }

            // 画光标
            if (i == currCursorPosition && shouldRenderCursor()) {
                float startX;
                if (textWidth == 0f) {
                    startX = offsetX + textGroupRect.centerX();
                } else {
                    startX = offsetX + textGroupRect.centerX() + textWidth / 2 + 1;
                }
                canvas.drawLine(startX, textGroupRect.top,
                        startX, textGroupRect.bottom,
                        cursorPaint);
            }
        }
        canvas.restore();
    }

    private boolean shouldRenderCursor() {
        if (!mCursorVisible) {
            return false;
        }

        if (!isFocused()) {
            return false;
        }

        if (currCursorPosition >= mTextGroupSize) {
            return false;
        }
        return (SystemClock.uptimeMillis() - mShowCursor) % (2 * BLINK) < BLINK;
    }

    public String getContent() {
        if (mTextGroup != null) {
            StringBuilder text = new StringBuilder();
            text.append(mTextGroup[0]);
            for (int i = 1; i < mTextGroup.length; i++) {
                if (!TextUtils.isEmpty(mSeparator)) {
                    text.append(mSeparator);
                }
                text.append(mTextGroup[i]);
            }

            return text.toString();
        }

        return null;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        // 重新计时
        mShowCursor = SystemClock.uptimeMillis();

        final int length = text.length();
        int increase = length - mLastTextLength;
        if (increase == 1) {
            char newChar = text.charAt(length - 1);
            if (mTextGroup == null) {
                return;
            }

            if (mTextGroup[currCursorPosition].length() >= mTextMaxLength
                    || mSeparator != null && mSeparator.charAt(0) == newChar) {
                currCursorPosition = Math.min(currCursorPosition + 1, mTextGroupSize - 1);
            } else {
                mTextGroup[currCursorPosition].append(newChar);
                if (mTextGroup[currCursorPosition].length() >= mTextMaxLength) {
                    currCursorPosition = Math.min(currCursorPosition + 1, mTextGroupSize - 1);
                }
            }
        } else if (increase > 1) {
            // 统一用数组处理，因为不含分隔符时也有可能达到单组字符数上限
            String[] newTextGroup;
            String newText = text.toString();
            if (newText.contains(mSeparator)) {
                newTextGroup = newText.split(".".equals(mSeparator) ? "\\." : mSeparator);
            } else {
                int size = length / mTextMaxLength + 1;
                newTextGroup = new String[size];
                for (int i = 0; i < size; i++) {
                    newTextGroup[i] = newText.substring(i * mTextMaxLength, Math.min((i + 1) * mTextMaxLength, length));
                }
            }

            for (int i = 0; i < newTextGroup.length; i++) {
                if (newTextGroup[i].length() > mTextMaxLength) {
                    break;
                }

                mTextGroup[currCursorPosition].append(newTextGroup[i]);
                if (i < newTextGroup.length - 1 || ((i == newTextGroup.length - 1) && newTextGroup[i].length() == mTextMaxLength)) {
                    // 最后一个除且长度没达到maxLength的除外
                    ++currCursorPosition;
                }
                if (currCursorPosition > mTextGroupSize) {
                    break;
                }
            }
            currCursorPosition = Math.min(currCursorPosition, mTextGroupSize - 1);
        }

        mLastTextLength = length;
    }

    private MyInputConnection myInputConnection = new MyInputConnection(null, true);

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        myInputConnection.setTarget(super.onCreateInputConnection(outAttrs));
        return myInputConnection;
    }

    public void setSeparator(String separator) {
        this.mSeparator = separator;
    }

    private class MyInputConnection extends InputConnectionWrapper {

        public MyInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }
        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    if (!TextUtils.isEmpty(mTextGroup[currCursorPosition])) {
                        int length = mTextGroup[currCursorPosition].length();
                        mTextGroup[currCursorPosition].delete(length - 1, length);
                        invalidate();
                        return true;
                    } else if (currCursorPosition != 0) {
                        --currCursorPosition;
                        invalidate();
                        return true;
                    }
                }
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean performEditorAction(int editorAction) {
            ++currCursorPosition;
            if (editorAction == 5) {
                if (currCursorPosition < mTextGroupSize) {
                    invalidate();
                    return true;
                } else {
                    currCursorPosition = -1;
                    invalidate();
                }
            }
            return super.performEditorAction(editorAction);
        }

    }

    private static float dp2px(int dp) {
        final float density = Resources.getSystem().getDisplayMetrics().density;
        return (density * dp + 0.5f);
    }
}