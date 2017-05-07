package com.poptech.poptalk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class SeekBarWaveformView extends View {

    private SeekBarWaveform seekBarWaveform;

    public SeekBarWaveformView(Context context) {
        super(context);
        init(context);
    }

    public SeekBarWaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SeekBarWaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        seekBarWaveform = new SeekBarWaveform(context);
        //seekBarWaveform.setColors(0xFFBBE3AC, 0xFF78C272, 0xFFA9DD96);
        seekBarWaveform.setColors(0xFF78C272, 0xFFF4B400 , 0xFFA9DD96);

    }

    public void setWaveform(byte[] waveform) {
        seekBarWaveform.setWaveform(waveform);
        invalidate();
    }

    public void setProgress(float progress) {
        seekBarWaveform.setProgress(progress);
        invalidate();
    }

    public boolean isDragging() {
        return seekBarWaveform.isDragging();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        seekBarWaveform.setSize(right - left, bottom - top);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        seekBarWaveform.draw(canvas);
    }
}