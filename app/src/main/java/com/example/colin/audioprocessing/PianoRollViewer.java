package com.example.colin.audioprocessing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.RemoteException;

import org.helllabs.android.xmp.R;
import com.example.colin.audioprocessing.ModInterface;


public class PianoRollViewer extends Viewer {
    private static final int MAX_NOTES = 96;
    private static final int MAX_CHANNELS = 64;
    private static final float NOTE_RADIUS_COEFFICIENT = 0.4f;
    private static final int DRAW_ALL_CHANNELS_CODE = -1;
    private static final boolean ROUNDED_RECTANGLES_ENABLED = false;
    private final Paint[] noteFillPaint = new Paint[MAX_CHANNELS];
    private final Paint[] noteOutlinePaint = new Paint[MAX_CHANNELS];
    private final Paint barPaint;
    private final int backgroundColor = Color.BLACK;
    private final byte[] rowNotes = new byte[64];
    private final byte[] rowInstruments = new byte[64];
    private int oldRow, oldOrd, oldPosX;
    private int channelToDraw = DRAW_ALL_CHANNELS_CODE;

    public PianoRollViewer(final Context context) {
        super(context);

        for (int channel = 0; channel < MAX_CHANNELS; channel += 8) {
            setupChannelPaint(channel + 0, getResources().getColor(R.color.track0_color));
            setupChannelPaint(channel + 1, getResources().getColor(R.color.track1_color));
            setupChannelPaint(channel + 2, getResources().getColor(R.color.track2_color));
            setupChannelPaint(channel + 3, getResources().getColor(R.color.track3_color));
            setupChannelPaint(channel + 4, getResources().getColor(R.color.track4_color));
            setupChannelPaint(channel + 5, getResources().getColor(R.color.track5_color));
            setupChannelPaint(channel + 6, getResources().getColor(R.color.track6_color));
            setupChannelPaint(channel + 7, getResources().getColor(R.color.track7_color));
        }

        barPaint = new Paint();
        barPaint.setARGB(50, 255, 255, 255);
    }

    @Override
    public void setup(final ModInterface modPlayer, final int[] modVars) {
        super.setup(modPlayer, modVars);

        oldRow = -1;
        oldOrd = -1;
        oldPosX = -1;
    }

    @Override
    public void update(final Info info, final boolean paused) {
        super.update(info, paused);

        final int row = info.values[2];
        final int ord = info.values[0];

        if (oldRow == row && oldOrd == ord && oldPosX == (int)posX) {
            return;
        }

        final int numRows = info.values[3];
        Canvas canvas = null;

        if (numRows != 0) {		// Skip first invalid infos
            oldRow = row;
            oldOrd = ord;
            oldPosX = (int)posX;
        }

        try {
            canvas = surfaceHolder.lockCanvas(null);
            if (canvas != null) {
                synchronized (surfaceHolder) {
                    doDraw(canvas, modPlayer, info);
                }
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void setupChannelPaint(int channel, int color) {
        noteFillPaint[channel] = new Paint();
        noteFillPaint[channel].setColor(color);
        noteFillPaint[channel].setAntiAlias(true);
        noteOutlinePaint[channel] = new Paint();
        noteOutlinePaint[channel].setColor(color);
        noteOutlinePaint[channel].setAntiAlias(true);
        noteOutlinePaint[channel].setStyle(Paint.Style.STROKE);
    }

    private void doDraw(final Canvas canvas, final ModInterface modPlayer, final Info info) {
        final int channelCount = modVars[3];
        final int currentPattern = info.values[1];
        final int currentRow = info.values[2];
        final int rowCount = info.values[3];
        final float noteHeight = (float) canvasHeight / MAX_NOTES;
        final float noteWidth = (float) canvasWidth / rowCount;
        final float noteRadius = NOTE_RADIUS_COEFFICIENT * Math.min(noteHeight, noteWidth);

        // Clear screen
        canvas.drawColor(backgroundColor);

        //Draw Notes
        for (int row = 0; row < rowCount; row++) {
            try {
                modPlayer.getPatternRow(currentPattern, row, rowNotes, rowInstruments);
            } catch (RemoteException e) { }

            for (int channel = 0; channel < channelCount; channel++) {
                if (!isMuted[channel] &&
                        (channelToDraw == DRAW_ALL_CHANNELS_CODE ||
                                channelToDraw % channelCount == channel)) {
                    int rowNote = rowNotes[channel] - 12;
                    if (rowNote != -12 && rowNote < MAX_NOTES) {
                        float left = row * noteWidth;
                        float top = (canvasHeight - noteHeight) - rowNote * noteHeight;

                        float scaleFactor = (float) info.volumes[channel] / 64;
                        if (row != currentRow) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ROUNDED_RECTANGLES_ENABLED) {
                                canvas.drawRoundRect(left, top, left + noteWidth, top + noteHeight,
                                        noteRadius, noteRadius, noteOutlinePaint[channel]);
                                canvas.drawRoundRect(makeScaledRectangle(left, top, noteWidth, noteHeight, scaleFactor),
                                        noteRadius, noteRadius, noteFillPaint[channel]);
                            } else {
                                canvas.drawRect(left, top, left + noteWidth, top + noteHeight,
                                        noteOutlinePaint[channel]);
                                canvas.drawRect(makeScaledRectangle(left, top, noteWidth, noteHeight, scaleFactor),
                                        noteFillPaint[channel]);
                            }
                        }
                    }
                }
            }
        }

        //Draw Position Marker Bar
        canvas.drawRect(currentRow * noteWidth, 0, currentRow * noteWidth + noteWidth, canvasHeight, barPaint);
    }

    private RectF makeScaledRectangle(float left, float top, float width, float height, float scaleFactor) {
        RectF rectF = new RectF(left, top, left + width * scaleFactor, top + height * scaleFactor);
        rectF.offset((width - rectF.width()) / 2, (height - rectF.height()) / 2);
        return rectF;
    }
}