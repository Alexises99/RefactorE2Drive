package com.example.refactore2drive.eyes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.example.refactore2drive.MainActivity;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class FaceTracker extends Tracker<Face> {
    private static final float EYE_CLOSED_THRESHOLD = 0.4f;
    private Context context;
    private GraphicOverlay mOverlay;
    private EyesGraphics mEyesGraphics;

    // Record the previously seen proportions of the landmark locations relative to the bounding box
    // of the face.  These proportions can be used to approximate where the landmarks are within the
    // face bounding box if the eye landmark is missing in a future update.
    @SuppressLint("UseSparseArrays")
    private Map<Integer, PointF> mPreviousProportions = new HashMap<>();

    // Similarly, keep track of the previous eye open state so that it can be reused for
    // intermediate frames which lack eye landmarks and corresponding eye state.
    private boolean mPreviousIsLeftOpen = true;
    private boolean mPreviousIsRightOpen = true;

    private boolean isLeftOpen, isRightOpen;

    private Thread thread;


    //==============================================================================================
    // Methods
    //==============================================================================================

    public FaceTracker(GraphicOverlay overlay, Context context) {
        this.context = context;
        mOverlay = overlay;
    }
    /**
     * Resets the underlying googly eyes graphic and associated physics state.
     */
    @Override
    public void onNewItem(int id, Face face) {
        mEyesGraphics = new EyesGraphics(mOverlay);
        Counter counter = new Counter();
        thread = new Thread(counter);
        thread.start();
        Log.d("FACE", "id; " +id + "Face: " + face.toString());
    }

    /**
     * Updates the positions and state of eyes to the underlying graphic, according to the most
     * recent face detection results.  The graphic will render the eyes and simulate the motion of
     * the iris based upon these changes over time.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        mOverlay.add(mEyesGraphics);


        updatePreviousProportions(face);

        PointF leftPosition = getLandmarkPosition(face, Landmark.LEFT_EYE);
        PointF rightPosition = getLandmarkPosition(face, Landmark.RIGHT_EYE);

        float leftOpenScore = face.getIsLeftEyeOpenProbability();
        if (leftOpenScore == Face.UNCOMPUTED_PROBABILITY) {
            isLeftOpen = mPreviousIsLeftOpen;
        } else {
            isLeftOpen = (leftOpenScore > EYE_CLOSED_THRESHOLD);
            mPreviousIsLeftOpen = isLeftOpen;
        }

        float rightOpenScore = face.getIsRightEyeOpenProbability();
        if (rightOpenScore == Face.UNCOMPUTED_PROBABILITY) {
            isRightOpen = mPreviousIsRightOpen;
        } else {
            isRightOpen = (rightOpenScore > EYE_CLOSED_THRESHOLD);
            mPreviousIsRightOpen = isRightOpen;
        }


        mEyesGraphics.updateEyes(leftPosition, isLeftOpen, rightPosition, isRightOpen);
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mEyesGraphics);
        if (thread != null) {
            if (thread.isAlive()) thread.interrupt();
        }
    }

    /**
     * Called when the face is assumed to be gone for good. Remove the googly eyes graphic from
     * the overlay.
     */
    @Override
    public void onDone() {
        mOverlay.remove(mEyesGraphics);
        if (thread != null) {
            if (thread.isAlive()) thread.interrupt();
        }
    }

    //==============================================================================================
    // Private
    //==============================================================================================

    private void updatePreviousProportions(Face face) {
        for (Landmark landmark : face.getLandmarks()) {
            PointF position = landmark.getPosition();
            float xProp = (position.x - face.getPosition().x) / face.getWidth();
            float yProp = (position.y - face.getPosition().y) / face.getHeight();
            mPreviousProportions.put(landmark.getType(), new PointF(xProp, yProp));
        }
    }

    /**
     * Finds a specific landmark position, or approximates the position based on past observations
     * if it is not present.
     */
    private PointF getLandmarkPosition(Face face, int landmarkId) {
        for (Landmark landmark : face.getLandmarks()) {
            if (landmark.getType() == landmarkId) {
                return landmark.getPosition();
            }
        }

        PointF prop = mPreviousProportions.get(landmarkId);
        if (prop == null) {
            return null;
        }

        float x = face.getPosition().x + (prop.x * face.getWidth());
        float y = face.getPosition().y + (prop.y * face.getHeight());
        return new PointF(x, y);
    }

    class Counter implements Runnable {
        @Override
        public void run() {
            while (true) {
                LocalTime startTime = LocalTime.now();
                int seconds = 0;
                while (seconds < 4 && isLeftOpen == false && isLeftOpen == false) {
                    try {
                        Thread.sleep(250);
                        Log.d("OJOS", "Ojo izq: " + isLeftOpen + " Ojo der: " + isLeftOpen);
                        Log.d("COUNTER", "El valor de seconds es: " + seconds);
                        LocalTime time = LocalTime.now();
                        seconds = Math.round(ChronoUnit.SECONDS.between(startTime, time));
                        if (isLeftOpen == false && isRightOpen == false && seconds >= 4) {
                            ((MainActivity) context).notifySound();
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                seconds = 0;
            }
        }
    }
}