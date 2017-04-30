package com.poptech.poptalk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.poptech.poptalk.sound.SamplePlayer;
import com.poptech.poptalk.utils.MetricUtils;


/**
 * Created by Administrator on 26/02/2017.
 */

public class AudioWaveFormTimelineView extends View {
    private Paint paint;
    private Paint paint2;
    private float progressLeft = 0;
    private float progressMidle = 0.5f;
    private float progressRight = 1;
    private boolean pressedLeft = false;
    private boolean pressedRight = false;
    private boolean pressedMiddle = false;

    private boolean chooseLeft = false;
    private boolean chooseRight = false;
    private boolean chooseMiddle = false;
    private float chooseDx = 0;

    private float pressDx = 0;
    private AudioWaveFormTimelineViewDelegate delegate = null;
    private Bitmap mBitmap;
    private SamplePlayer mPlayer;

    public interface AudioWaveFormTimelineViewDelegate {
        void onLeftProgressChanged(float progress);
        void onMidleProgressChanged(float progress);
        void onRightProgressChanged(float progress);
    }

    public AudioWaveFormTimelineView(Context context) {
        super(context);
        init(context);
    }
    public AudioWaveFormTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioWaveFormTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context) {
        paint = new Paint();
        paint.setColor(0xffBDBDBD);
        paint2 = new Paint();
        paint2.setColor(0x7f000000);
    }

    public void setBackground(Bitmap bitmap){
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
        int startX = (int)(width * progressLeft);
        int midX = (int)(width * progressMidle);
        int endX = (int)(width * progressRight);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int additionWidth = MetricUtils.dpToPx(30);
            if (startX - additionWidth <= x && x <= startX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedLeft = true;
                pressDx = (int)(x - startX);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else if (endX - additionWidth <= x && x <= endX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedRight = true;
                pressDx = (int)(x - endX);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            }else if (midX - additionWidth <= x && x <= midX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedMiddle = true;
                pressDx = (int)(x - midX);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            }else{
                if(x <= startX - additionWidth){
                    chooseDx = x;
                    chooseLeft = true;
                    return true;
                }else if(x >= startX + additionWidth && x <= endX - additionWidth){
                    chooseDx = x;
                    chooseMiddle = true;
                    return true;
                }else if(x >= endX + additionWidth){
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
            }else if (pressedMiddle) {
                pressedMiddle = false;
                return true;
            }else{
                if (chooseLeft) {
                    chooseLeft = false;
                    startX = (int) x;
                    progressLeft = (float)(startX) / (float)width;
                    if (delegate != null) {
                        delegate.onLeftProgressChanged(progressLeft);
                    }
                    invalidate();
                    return true;
                } else if (chooseRight) {
                    chooseRight = false;
                    endX = (int) x;
                    progressRight = (float)(endX) / (float)width;
                    if (delegate != null) {
                        delegate.onRightProgressChanged(progressRight);
                    }
                    invalidate();
                    return true;
                }else if (chooseMiddle) {
                    chooseMiddle = false;
                    midX = (int) x;
                    progressMidle = (float)(midX) / (float)width;
                    if (delegate != null) {
                        delegate.onMidleProgressChanged(progressMidle);
                    }
                    invalidate();
                    return true;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressedLeft) {
                startX = (int)(x - pressDx);
                if (startX < 0) {
                    startX = 0;
                } else if (startX > midX) {
                    startX = midX;
                }
                progressLeft = (float)(startX) / (float)width;
                if (delegate != null) {
                    delegate.onLeftProgressChanged(progressLeft);
                }
                invalidate();
                return true;
            } else if (pressedRight) {
                endX = (int)(x - pressDx);
                if (endX < midX) {
                    endX = midX;
                } else if (endX > width) {
                    endX = width;
                }
                progressRight = (float)(endX) / (float)width;
                if (delegate != null) {
                    delegate.onRightProgressChanged(progressRight);
                }
                invalidate();
                return true;
            }else if (pressedMiddle) {
                midX = (int)(x - pressDx);
                if (midX < startX) {
                    midX = startX;
                } else if ( midX> endX) {
                    midX = endX;
                }
                progressMidle = (float)(midX) / (float)width;
                if (delegate != null) {
                    delegate.onMidleProgressChanged(progressMidle);
                }
                invalidate();
                return true;
            }
        }
        return false;
    }

    public void setDelegate(AudioWaveFormTimelineViewDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth()- MetricUtils.dpToPx(36);
        int height = getMeasuredHeight();
        int startX = (int)(width * progressLeft) + MetricUtils.dpToPx(16);
        int midX = (int)(width * progressMidle) + MetricUtils.dpToPx(16);
        int endX = (int)(width * progressRight) + MetricUtils.dpToPx(16);

        canvas.save();
        canvas.clipRect(MetricUtils.dpToPx(16), 0, width + MetricUtils.dpToPx(20), height);

        canvas.drawRect(MetricUtils.dpToPx(16), MetricUtils.dpToPx(2), startX, height - MetricUtils.dpToPx(2), paint2);
        canvas.drawRect(endX + MetricUtils.dpToPx(4), MetricUtils.dpToPx(2), MetricUtils.dpToPx(16) + width + MetricUtils.dpToPx(4), height - MetricUtils.dpToPx(2), paint2);

        canvas.drawRect(startX, 0, startX + MetricUtils.dpToPx(2), height, paint);
        canvas.drawRect(midX, 0, midX + MetricUtils.dpToPx(2), height, paint);
        canvas.drawRect(endX + MetricUtils.dpToPx(2), 0, endX + MetricUtils.dpToPx(4), height, paint);
        canvas.drawRect(startX + MetricUtils.dpToPx(2), 0, endX + MetricUtils.dpToPx(4), MetricUtils.dpToPx(2), paint);
        canvas.drawRect(startX + MetricUtils.dpToPx(2), height - MetricUtils.dpToPx(2), endX + MetricUtils.dpToPx(4), height, paint);
        canvas.restore();

        canvas.drawCircle(startX, getMeasuredHeight() / 2, MetricUtils.dpToPx(8), paint);
        canvas.drawCircle(midX, getMeasuredHeight() / 2, MetricUtils.dpToPx(8), paint);
        canvas.drawCircle(endX + MetricUtils.dpToPx(2), getMeasuredHeight() / 2, MetricUtils.dpToPx(8), paint);
    }

    public void setProgressLeft(float progressLeft) {
        this.progressLeft = progressLeft;
    }

    public void setProgressMidle(float progressMidle) {
        this.progressMidle = progressMidle;
    }

    public void setProgressRight(float progressRight) {
        this.progressRight = progressRight;
    }
}
