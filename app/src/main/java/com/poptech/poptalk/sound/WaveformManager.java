package com.poptech.poptalk.sound;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;


import com.poptech.poptalk.R;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sontt on 26/02/2017.
 */

public class WaveformManager {
    private Paint mUnselectedLinePaint;
    private SoundFile mSoundFile;
    private int[] mLenByZoomLevel;
    private double[][] mValuesByZoomLevel;
    private double[] mZoomFactorByZoomLevel;
    private int[] mHeightsAtThisZoomLevel;
    private int mZoomLevel;
    private int mSampleRate;
    private int mSamplesPerFrame;
    private int mOffset;

    private boolean mInitialized;

    public WaveformManager(Context context) {
        Resources res = context.getResources();
        mUnselectedLinePaint = new Paint();
        mUnselectedLinePaint.setAntiAlias(false);
        mUnselectedLinePaint.setColor(res.getColor(R.color.waveform_unselected));

        mSoundFile = null;
        mLenByZoomLevel = null;
        mValuesByZoomLevel = null;
        mHeightsAtThisZoomLevel = null;
        mOffset = 0;
        mInitialized = false;
    }

    public boolean hasSoundFile() {
        return mSoundFile != null;
    }

    public void setSoundFile(SoundFile soundFile) {
        mSoundFile = soundFile;
        mSampleRate = mSoundFile.getSampleRate();
        mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
        computeDoublesForAllZoomLevels();
        mHeightsAtThisZoomLevel = null;
    }

    public boolean isInitialized() {
        return mInitialized;
    }


    public double pixelsToSeconds(int pixels) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (pixels * (double) mSamplesPerFrame / (mSampleRate * z));
    }

    protected void drawWaveformLine(Canvas canvas,
                                    int x, int y0, int y1,
                                    Paint paint) {
        canvas.drawLine(x, y0, x, y1, paint);
    }

    public Bitmap drawBitmap(int height) {
        if (mSoundFile == null)
            return null;

        if (mHeightsAtThisZoomLevel == null)
            computeIntsForThisZoomLevel(height);

        int start = 0;
        int width = mHeightsAtThisZoomLevel.length - start;
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        // Draw waveform
        int ctr = height / 2;
        double onePixelInSecs = pixelsToSeconds(1);
        double fractionalSecs = mOffset * onePixelInSecs;
        int i = 0;

        // Draw waveform
        for (i = 0; i < width; i++) {
            Paint paint;

            paint = mUnselectedLinePaint;
            drawWaveformLine(
                    canvas, i,
                    ctr - mHeightsAtThisZoomLevel[start + i],
                    ctr + 1 + mHeightsAtThisZoomLevel[start + i],
                    paint);

        }

        saveBitmaptoFile(b, Environment.getExternalStorageDirectory() + "/son.png");
        Log.e("sontt", "in here");
        return b;
    }

    public void saveBitmaptoFile(Bitmap bmp, String filename) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called once when a new sound file is added
     */
    private void computeDoublesForAllZoomLevels() {
        int numFrames = mSoundFile.getNumFrames();
        int[] frameGains = mSoundFile.getFrameGains();
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0];
            smoothedGains[1] = frameGains[1];
        } else if (numFrames > 2) {
            smoothedGains[0] = (double) (
                    (frameGains[0] / 2.0) +
                            (frameGains[1] / 2.0));
            for (int i = 1; i < numFrames - 1; i++) {
                smoothedGains[i] = (double) (
                        (frameGains[i - 1] / 3.0) +
                                (frameGains[i] / 3.0) +
                                (frameGains[i + 1] / 3.0));
            }
            smoothedGains[numFrames - 1] = (double) (
                    (frameGains[numFrames - 2] / 2.0) +
                            (frameGains[numFrames - 1] / 2.0));
        }

        // Make sure the range is no more than 0 - 255
        double maxGain = 1.0;
        for (int i = 0; i < numFrames; i++) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i];
            }
        }
        double scaleFactor = 1.0;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int) (smoothedGains[i] * scaleFactor);
            if (smoothedGain < 0)
                smoothedGain = 0;
            if (smoothedGain > 255)
                smoothedGain = 255;

            if (smoothedGain > maxGain)
                maxGain = smoothedGain;

            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        double minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int) minGain];
            minGain++;
        }

        // Re-calibrate the max to be 99%
        sum = 0;
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int) maxGain];
            maxGain--;
        }

        // Compute the heights
        double[] heights = new double[numFrames];
        double range = maxGain - minGain;
        for (int i = 0; i < numFrames; i++) {
            double value = (smoothedGains[i] * scaleFactor - minGain) / range;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights[i] = value * value;
        }

        mLenByZoomLevel = new int[1];
        mZoomFactorByZoomLevel = new double[1];
        mValuesByZoomLevel = new double[1][];

        // Level 0 is doubled, with interpolated values
        mZoomFactorByZoomLevel[0] = getFactoryZoomByLevel(heights, 1080);
        mValuesByZoomLevel[0] = getValueZoomByLevel(heights, 1080);
        mLenByZoomLevel[0] = mValuesByZoomLevel[0].length;
        mZoomLevel = 0;

        mInitialized = true;
    }

    /**
     * Called the first time we need to draw when the zoom level has changed
     * or the screen is resized
     */
    private void computeIntsForThisZoomLevel(int height) {
        int halfHeight = (height / 2) - 1;
        mHeightsAtThisZoomLevel = new int[mLenByZoomLevel[mZoomLevel]];
        for (int i = 0; i < mLenByZoomLevel[mZoomLevel]; i++) {
            mHeightsAtThisZoomLevel[i] =
                    (int) (mValuesByZoomLevel[mZoomLevel][i] * halfHeight);
        }
    }

    public double[] getValueZoomByLevel(double[] heights, long screenSize) {
        double[] retValue, nextValue;

        retValue = new double[heights.length];
        System.arraycopy(heights, 0, retValue, 0, retValue.length);

        /* Return value with size smaller than scree size */
        while ((retValue.length * 2) < (screenSize * 1.5)) {
            nextValue = new double[retValue.length * 2];
            // TODO: Implement heights value
            for (int i = 0; i < retValue.length; i++) {
                if (i != 0)
                    nextValue[i * 2] = 0.5 * (retValue[i - 1] + retValue[i]);
                else
                    nextValue[i] = 0.5 * retValue[i];
                nextValue[i * 2 + 1] = retValue[i];
            }
            // Set to return value
            retValue = new double[nextValue.length];
            System.arraycopy(nextValue, 0, retValue, 0, retValue.length);
        }

        /* Return value is larger than screen size * 1.5 */
        // Return value must be smaller than screen size * 1.5
        while (retValue.length > (screenSize * 1.5)) {
            nextValue = new double[retValue.length / 2];
            // TODO: Implement heights value
            for (int i = 0; i < nextValue.length; i++) {
                nextValue[i] = 0.5 * (retValue[i * 2] + retValue[i * 2 + 1]);
            }
            retValue = new double[nextValue.length];
            System.arraycopy(nextValue, 0, retValue, 0, retValue.length);
        }
        return retValue;
    }

    public double getFactoryZoomByLevel(double[] heights, long screenSize) {
        double curentValue = heights.length;
        double retFactory = 1.0;

        while ((curentValue * 2) < (screenSize * 1.5)) {
            curentValue = curentValue * 2;
            retFactory = retFactory * 2;
        }
        while (curentValue > (screenSize * 1.5)) {
            curentValue = curentValue / 2;
            retFactory = retFactory / 2;
        }
        return retFactory;
    }
}
