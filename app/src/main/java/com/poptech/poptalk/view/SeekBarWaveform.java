/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package com.poptech.poptalk.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.poptech.poptalk.utils.AndroidUtilities;


public class SeekBarWaveform {

    private static Paint paintInner;
    private static Paint paintOuter;
    private int thumbLeftX = 0;
    private int thumbRightX = 0;
    private int thumbDX = 0;
    private float startX;
    private boolean startDraging = false;
    private boolean pressed = false;
    private int width;
    private int height;
    private SeekBar.SeekBarDelegate delegate;
    private byte[] waveformBytes;
    private View parentView;
    private boolean selected;
    private Context mContext;

    private int innerColor;
    private int outerColor;
    private int selectedColor;

    public SeekBarWaveform(Context context) {
        if (paintInner == null) {
            paintInner = new Paint();
            paintOuter = new Paint();
        }
        this.mContext = context;
    }

    public void setDelegate(SeekBar.SeekBarDelegate seekBarDelegate) {
        delegate = seekBarDelegate;
    }

    public void setColors(int inner, int outer, int selected) {
        innerColor = inner;
        outerColor = outer;
        selectedColor = selected;
    }

    public void setWaveform(byte[] waveform) {
        waveformBytes = waveform;
    }

    public void setSelected(boolean value) {
        selected = value;
    }

    public void setParentView(View view) {
        parentView = view;
    }

    public boolean isStartDraging() {
        return startDraging;
    }

    public boolean onTouch(int action, float x, float y) {
        if (action == MotionEvent.ACTION_DOWN) {
            if (0 <= x && x <= width && y >= 0 && y <= height) {
                startX = x;
                pressed = true;
                thumbDX = (int) (x - thumbLeftX);
                startDraging = false;
                return true;
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (pressed) {
                if (action == MotionEvent.ACTION_UP && delegate != null) {
                    delegate.onSeekBarDrag((float) thumbLeftX / (float) width);
                }
                pressed = false;
                return true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (pressed) {
                if (startDraging) {
                    thumbLeftX = (int) (x - thumbDX);
                    if (thumbLeftX < 0) {
                        thumbLeftX = 0;
                    } else if (thumbLeftX > width) {
                        thumbLeftX = width;
                    }
                }
                if (startX != -1 && Math.abs(x - startX) > AndroidUtilities.getPixelsInCM((Activity) mContext, 0.2f, true)) {
                    if (parentView != null && parentView.getParent() != null) {
                        parentView.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    startDraging = true;
                    startX = -1;
                }
                return true;
            }
        }
        return false;
    }

    public void setLeftProgress(float progress) {
        thumbLeftX = (int) Math.ceil(width * progress);
        if (thumbLeftX < 0) {
            thumbLeftX = 0;
        } else if (thumbLeftX > width) {
            thumbLeftX = width;
        }
    }

    public void setRightProgress(float progress) {
        thumbRightX = (int) Math.ceil(width * progress);
        if (thumbRightX < 0) {
            thumbRightX = 0;
        } else if (thumbRightX > width) {
            thumbRightX = width;
        }
    }

    public boolean isDragging() {
        return pressed;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        thumbLeftX = 0;
        thumbRightX = w;
    }

    public void draw(Canvas canvas) {
        int colWidth = 3;
        int colHeight = 90;
        int colSpace = 4;

        if (waveformBytes == null || width == 0) {
            return;
        }
        float totalBarsCount = width / AndroidUtilities.dp(colSpace);
        if (totalBarsCount <= 0.1f) {
            return;
        }
        byte value;
        int samplesCount = (waveformBytes.length * 8 / 5);
        float samplesPerBar = samplesCount / totalBarsCount;
        float barCounter = 0;
        int nextBarNum = 0;

        paintInner.setColor((selected ? selectedColor : innerColor));
        paintOuter.setColor(outerColor);

        int y = (height - AndroidUtilities.dp(colHeight));
        int barNum = 0;
        int lastBarNum;
        int drawBarCount;

        for (int a = 0; a < samplesCount; a++) {
            if (a != nextBarNum) {
                continue;
            }
            drawBarCount = 0;
            lastBarNum = nextBarNum;
            while (lastBarNum == nextBarNum) {
                barCounter += samplesPerBar;
                nextBarNum = (int) barCounter;
                drawBarCount++;
            }

            int bitPointer = a * 5;
            int byteNum = bitPointer / 8;
            int byteBitOffset = bitPointer - byteNum * 8;
            int currentByteCount = 8 - byteBitOffset;
            int nextByteRest = 5 - currentByteCount;
            value = (byte) ((waveformBytes[byteNum] >> byteBitOffset) & ((2 << (Math.min(5, currentByteCount) - 1)) - 1));
            if (nextByteRest > 0) {
                value <<= nextByteRest;
                value |= waveformBytes[byteNum + 1] & ((2 << (nextByteRest - 1)) - 1);
            }

            for (int b = 0; b < drawBarCount; b++) {
                int x = barNum * AndroidUtilities.dp(colSpace);
                if (x > thumbRightX) {
                    canvas.drawRect(x, y + AndroidUtilities.dp(colHeight - Math.max(1, colHeight * 1.0f * value / 31.0f)), x + AndroidUtilities.dp(colWidth), y + AndroidUtilities.dp(colHeight), paintOuter);
                } else if (x < thumbLeftX && x + AndroidUtilities.dp(colWidth) < thumbLeftX) {
                    canvas.drawRect(x, y + AndroidUtilities.dp(colHeight - Math.max(1, colHeight * 1.0f * value / 31.0f)), x + AndroidUtilities.dp(colWidth), y + AndroidUtilities.dp(colHeight), paintOuter);
                } else {
                    canvas.drawRect(x, y + AndroidUtilities.dp(colHeight - Math.max(1, colHeight * 1.0f * value / 31.0f)), x + AndroidUtilities.dp(colWidth), y + AndroidUtilities.dp(colHeight), paintInner);
                    if (x + AndroidUtilities.dp(colWidth) > thumbRightX) {
                        canvas.drawRect(thumbRightX, y + AndroidUtilities.dp(colHeight - Math.max(1, colHeight * 1.0f * value / 31.0f)), x + AndroidUtilities.dp(colWidth), y + AndroidUtilities.dp(colHeight), paintOuter);
                    }
                    if (x < thumbLeftX) {
                        canvas.drawRect(x, y + AndroidUtilities.dp(colHeight - Math.max(1, colHeight * 1.0f * value / 31.0f)), thumbLeftX, y + AndroidUtilities.dp(colHeight), paintOuter);
                    }
                }
                barNum++;
            }
        }
    }
}
