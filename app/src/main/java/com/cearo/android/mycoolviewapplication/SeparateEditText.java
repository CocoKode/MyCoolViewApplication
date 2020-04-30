package com.cearo.android.mycoolviewapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    float textWidthCache = 10f;
    // 字符组的个数
    private int maxTextGroupSize = 4;
    // 每个字符组的最大可容纳字符个数
    private int maxTextLength = 5;
    // 字符组之间的间隔
    private float interval = dp2px(8);
    // 每个bottomLine上的字符
    private StringBuilder[] textGroup;
    // 光标显示时长
    private static final int BLINK = 500;
    // 光标开始显示的时间
    private static long mShowCursor;
    // 当前光标所在位置
    private int currCursorPosition = 0;
    private int selectedBottomLineWidth = 3;
    private final float bottomLineOffset = dp2px(7);
    private volatile boolean cursorVisible = true;
    private int lastTextLength = 0;

    private String separator;

    private Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint bottomLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);


    public SeparateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackground(null);
        bottomLinePaint.setColor(Color.GREEN);
        bottomLinePaint.setStyle(Paint.Style.FILL);
        bottomLinePaint.setStrokeWidth(1);

        cursorPaint.setColor(Color.BLACK);
        cursorPaint.setStyle(Paint.Style.FILL);
        cursorPaint.setStrokeWidth(1);

        textPaint.setTextSize(getTextSize());

        textGroup = new StringBuilder[maxTextGroupSize];
        for (int i = 0; i < maxTextGroupSize; i++) {
            textGroup[i] = new StringBuilder(maxTextLength);
        }
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
        textGroupRect.right = (getWidth() - getPaddingLeft() - getPaddingRight() - interval * (maxTextGroupSize - 1)) / maxTextGroupSize;
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
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        // 防止内容左移
        setScrollX(0);
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
    }

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
            return (int) (x / (textGroupRect.width() + interval));
        }

        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        float padding = getPaddingLeft();
        canvas.translate(padding, 0);

        for (int i = 0; i < maxTextGroupSize; i++) {
            float offsetX = i * (textGroupRect.right + interval);

            // 画底边
            if (i == currCursorPosition) {
                bottomLinePaint.setStrokeWidth(selectedBottomLineWidth);
            } else {
                bottomLinePaint.setStrokeWidth(1);
            }
            canvas.drawLine(offsetX + textGroupRect.left, contentRect.bottom - bottomLineOffset,
                    offsetX + textGroupRect.right, contentRect.bottom - bottomLineOffset,
                    bottomLinePaint);

            // 画文字
            float textWidth = 0f;
            if (textGroup[i] != null && !TextUtils.isEmpty(textGroup[i].toString())) {
                String text = textGroup[i].toString();
                int textLength = text.length();
                textWidth = textWidthCache * textLength;
                canvas.drawText(text, offsetX + textGroupRect.centerX() - textWidth / 2, textGroupRect.bottom - 3, textPaint);
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
        if (!cursorVisible) {
            return false;
        }

        if (!isFocused()) {
            return false;
        }

        if (currCursorPosition >= maxTextGroupSize) {
            return false;
        }
        return (SystemClock.uptimeMillis() - mShowCursor) % (2 * BLINK) < BLINK;
    }

    public String getContent() {
        if (textGroup != null) {
            StringBuilder text = new StringBuilder();
            text.append(textGroup[0]);
            for (int i = 1; i < textGroup.length; i++) {
                text.append(".").append(textGroup[i]);
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
        int increase = length - lastTextLength;
        if (increase == 1) {
            String newText = text.subSequence(length - 1, length).toString();
            if (textGroup == null) {
                return;
            }

            if (textGroup[currCursorPosition].length() >= maxTextLength
                    || (separator != null && separator.equals(newText)) ) {
                currCursorPosition = Math.min(currCursorPosition + 1, maxTextGroupSize - 1);
            } else {
                textGroup[currCursorPosition].append(newText);
            }
        } else if (increase > 1) {
            // 统一用数组处理，因为不含分隔符时也有可能达到单组字符数上限
            String[] newTextGroup;
            String newText = text.toString();
            if (newText.contains(separator)) {
                newTextGroup = newText.split(".".equals(separator) ? "\\." : separator);
            } else {
                int size = length / maxTextLength + 1;
                newTextGroup = new String[size];
                for (int i = 0; i < size; i++) {
                    newTextGroup[i] = newText.substring(i * maxTextLength, Math.min((i + 1) * maxTextLength, length));
                }
            }

            for (int i = 0; i < newTextGroup.length; i++) {
                if (newTextGroup[i].length() > maxTextLength) {
                    break;
                }

                textGroup[currCursorPosition].append(newTextGroup[i]);
                if (i < newTextGroup.length - 1 || ((i == newTextGroup.length - 1) && newTextGroup[i].length() == maxTextLength)) {
                    // 最后一个除且长度没达到maxLength的除外
                    ++currCursorPosition;
                }
                if (currCursorPosition > maxTextGroupSize) {
                    break;
                }
            }
            currCursorPosition = Math.min(currCursorPosition, maxTextGroupSize - 1);
        }

        lastTextLength = length;
    }

    private MyInputConnection myInputConnection = new MyInputConnection(null, true);

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        myInputConnection.setTarget(super.onCreateInputConnection(outAttrs));
        return myInputConnection;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    private class MyInputConnection extends InputConnectionWrapper {

        public MyInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }
        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    if (!TextUtils.isEmpty(textGroup[currCursorPosition])) {
                        int length = textGroup[currCursorPosition].length();
                        textGroup[currCursorPosition].delete(length - 1, length);
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
                if (currCursorPosition < maxTextGroupSize) {
                    return true;
                } else {
                    currCursorPosition = -1;
                }

                invalidate();
            }
            return super.performEditorAction(editorAction);
        }

    }

    private float dp2px(int dp) {
        final float density = getResources().getDisplayMetrics().density;
        return (density * dp + 0.5f);
    }
}
