/*
 * Copyright 2019 Atri Tripathi. All rights reserved.
 */

package com.atritripathi.notes.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;

public class LinedEditText extends AppCompatEditText {
    public static final int NUM_NOTE_LINES = 100;
    private Rect mRect;
    private Paint mPaint;

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            mPaint.setColor(0x33F9F9F9);
        } else {
            mPaint.setColor(0xFFFFD966);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setPadding(12,12,12,12);
        int height = NUM_NOTE_LINES * ((View)this.getParent()).getHeight();
        int lineHeight = getLineHeight();
        int numberOfLines = height / lineHeight;


        Rect rect = mRect;
        Paint paint = mPaint;

        int baseLine = getLineBounds(0, rect);

        for (int i = 0; i < numberOfLines; i++) {
            canvas.drawLine(rect.left,baseLine + 4, rect.right, baseLine + 4, paint);
            baseLine += lineHeight;
        }
        super.onDraw(canvas);
    }
}
