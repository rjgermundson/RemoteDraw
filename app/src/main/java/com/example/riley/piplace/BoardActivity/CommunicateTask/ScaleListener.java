package com.example.riley.piplace.BoardActivity.CommunicateTask;

import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.riley.piplace.BoardActivity.ScaledBoard;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private static float MAX_SCALE = 4.0f;
    private static float MIN_SCALE = 0.4f;
    private ScaledBoard board;

    public ScaleListener(ScaledBoard board) {
        super();
        this.board = board;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float scaleFactor = scaleGestureDetector.getScaleFactor();
        scaleFactor = Math.min(scaleFactor, MAX_SCALE);
        scaleFactor = Math.max(scaleFactor, MIN_SCALE);
        board.setScale(scaleFactor);
        return true;
    }
}
