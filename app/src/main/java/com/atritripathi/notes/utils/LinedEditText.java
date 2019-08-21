package com.atritripathi.notes.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

public class LinedEditText extends AppCompatEditText {
    private Rect mRect;
    private Paint mPaint;

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(0xFFFFD966);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setPadding(8,8,8,8);
        int height = 100 * ((View)this.getParent()).getHeight();
        int lineHeight = getLineHeight();
        int numberOfLines = height / lineHeight;

        Rect rect = mRect;
        Paint paint = mPaint;

        int baseLine = getLineBounds(0, rect);

        for (int i = 0; i < numberOfLines; i++) {
            canvas.drawLine(rect.left,baseLine + 1, rect.right, baseLine + 1, paint);
            baseLine += lineHeight;
        }

        super.onDraw(canvas);
    }
}
