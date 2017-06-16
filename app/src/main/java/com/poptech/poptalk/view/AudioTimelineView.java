package com.poptech.poptalk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.poptech.poptalk.sound.SamplePlayer;
import com.poptech.poptalk.utils.MetricUtils;


/**
 * Created by Administrator on 26/02/2017.
 */

public class AudioTimelineView extends View {
    private Paint paint;
    private Paint paint2;
    private Paint paint3;
    private float progressLeft = 0;
    private float progressMiddle = 0.5f;
    private float progressRight = 1;
    private boolean pressedLeft = false;
    private boolean pressedRight = false;
    private boolean pressedMiddle = false;

    private boolean chooseLeft = false;
    private boolean chooseRight = false;
    private boolean chooseMiddle = false;
    private float chooseDx = 0;

    private float pressDx = 0;
    private AudioTimelineDelegate delegate = null;
    private Bitmap mBitmap;
    private SamplePlayer mPlayer;

    public interface AudioTimelineDelegate {
        void onLeftProgressChanged(float progress);

        void onMiddleProgressChanged(float progress);

        void onRightProgressChanged(float progress);
    }

    public AudioTimelineView(Context context) {
        super(context);
        init(context);
    }

    public AudioTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setColor(0xffBDBDBD);
        paint2 = new Paint();
        paint2.setColor(0x3f000000);
        paint3 = new Paint();
        paint3.setColor(0x3f000000);
        paint3.setTextSize(40);
        paint3.setStyle(Paint.Style.FILL);
    }

    public void setBackground(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();

        int width = getMeasuredWidth();
        int startX = (int) (width * progressLeft);
        int midX = (int) (width * progressMiddle);
        int endX = (int) (width * progressRight);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int additionWidth = MetricUtils.dpToPx(30);
            if (startX - additionWidth <= x && x <= startX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedLeft = true;
                pressDx = (int) (x - startX);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else if (endX - additionWidth <= x && x <= endX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedRight = true;
                pressDx = (int) (x - endX);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else if (midX - additionWidth <= x && x <= midX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedMiddle = true;
                pressDx = (int) (x - midX);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else {
                if (x <= startX - additionWidth) {
                    chooseDx = x;
                    chooseLeft = true;
                    return true;
                } else if (x >= startX + additionWidth && x <= endX - additionWidth) {
                    chooseDx = x;
                    chooseMiddle = true;
                    return true;
                } else if (x >= endX + additionWidth) {
                    chooseDx = x;
                    chooseRight = true;
                    return true;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (pressedLeft) {
                pressedLeft = false;
                return true;
            } else if (pressedRight) {
                pressedRight = false;
                return true;
            } else if (pressedMiddle) {
                pressedMiddle = false;
                return true;
            } else {
                if (chooseLeft) {
                    chooseLeft = false;
                    startX = (int) x;
                    progressLeft = (float) (startX) / (float) width;
                    if (delegate != null) {
                        delegate.onLeftProgressChanged(progressLeft);
                    }
                    invalidate();
                    return true;
                } else if (chooseRight) {
                    chooseRight = false;
                    endX = (int) x;
                    progressRight = (float) (endX) / (float) width;
                    if (delegate != null) {
                        delegate.onRightProgressChanged(progressRight);
                    }
                    invalidate();
                    return true;
                } else if (chooseMiddle) {
                    chooseMiddle = false;
                    midX = (int) x;
                    progressMiddle = (float) (midX) / (float) width;
                    if (delegate != null) {
                        delegate.onMiddleProgressChanged(progressMiddle);
                    }
                    invalidate();
                    return true;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressedLeft) {
                startX = (int) (x - pressDx);
                if (startX < 0) {
                    startX = 0;
                } else if (startX > midX) {
                    startX = midX;
                }
                progressLeft = (float) (startX) / (float) width;
                if (delegate != null) {
                    delegate.onLeftProgressChanged(progressLeft);
                }
                invalidate();
                return true;
            } else if (pressedRight) {
                endX = (int) (x - pressDx);
                if (endX < midX) {
                    endX = midX;
                } else if (endX > width) {
                    endX = width;
                }
                progressRight = (float) (endX) / (float) width;
                if (delegate != null) {
                    delegate.onRightProgressChanged(progressRight);
                }
                invalidate();
                return true;
            } else if (pressedMiddle) {
                midX = (int) (x - pressDx);
                if (midX < startX) {
                    midX = startX;
                } else if (midX > endX) {
                    midX = endX;
                }
                progressMiddle = (float) (midX) / (float) width;
                if (delegate != null) {
                    delegate.onMiddleProgressChanged(progressMiddle);
                }
                invalidate();
                return true;
            }
        }
        return false;
    }

    public void setDelegate(AudioTimelineDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight() - MetricUtils.dpToPx(15);
        int startX = (int) (width * progressLeft);
        int midX = (int) (width * progressMiddle);
        int endX = (int) (width * progressRight);

        canvas.save();
        canvas.clipRect(0, 0, getMeasuredWidth(), getMeasuredWidth());
        canvas.drawRect(0, 0, startX, height, paint2);
        canvas.drawRect(endX, 0, width, height, paint2);

        canvas.drawRect(startX, 0, startX + MetricUtils.dpToPx(2), height, paint);
        canvas.drawRect(midX - MetricUtils.dpToPx(1), 0, midX + MetricUtils.dpToPx(1), height, paint);
        canvas.drawRect(endX - MetricUtils.dpToPx(2), 0, endX, height, paint);
        canvas.drawRect(startX + MetricUtils.dpToPx(1), 0, endX - MetricUtils.dpToPx(1), MetricUtils.dpToPx(0), paint);
        canvas.drawRect(startX + MetricUtils.dpToPx(1), height - MetricUtils.dpToPx(0), endX - MetricUtils.dpToPx(1), height, paint);

        String startText = "A";
        canvas.drawText(startText, startX - paint3.measureText(startText) / 2, getMeasuredHeight(), paint3);

        String midText = "B";
        canvas.drawText(midText, midX - paint3.measureText(midText) / 2, getMeasuredHeight(), paint3);

        String endText = "C";
        canvas.drawText(endText, endX - paint3.measureText(endText) / 2, getMeasuredHeight(), paint3);

        canvas.restore();
    }

    public void setProgressLeft(float progressLeft) {
        this.progressLeft = progressLeft;
    }

    public void setProgressMiddle(float progressMiddle) {
        this.progressMiddle = progressMiddle;
    }

    public void setProgressRight(float progressRight) {
        this.progressRight = progressRight;
    }
}
