package com.arover.camera;

import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minstrel
 *         created at 19/07/2017 17:06
 */

public class CameraHandler extends HandlerThread {

    private static final String TAG = "CameraHandler";

    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

    static {
        FLASH_MODES.put(Config.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MODES.put(Config.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
        FLASH_MODES.put(Config.FLASH_TORCH, Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MODES.put(Config.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MODES.put(Config.FLASH_RED_EYE, Camera.Parameters.FLASH_MODE_RED_EYE);
    }

    private static final int MSG_START = 1;
    private static final int MSG_STOP = 2;
    private static final int MSG_CAPTURE = 3;
    private static final int MSG_SWITCH_FACING = 4;
    private static final int MSG_QUIT = 5;

    private Callback callback;
    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();

    private Camera camera;
    private Handler handler;
    private boolean isPrepared;
    private boolean mAutoFocus;
    private Camera.Parameters mCameraParameters;
    private AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);
    private int mFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int mCameraId;
    private int INVALID_CAMERA_ID = -1;
    private int mDisplayOrientation;
    private int mFlash;
    private SurfaceHolder holder;
    private boolean setAfterOrientationInit;
    private boolean pendingStartPreview;
    private AtomicBoolean isOpenCamera = new AtomicBoolean(false);


    public CameraHandler(Callback callback) {
        super("CameraHandler");
        this.callback = callback;
    }

    public void removeCallback() {
        this.callback = null;
    }

    public void performCommand(int command) {
        if (!isPrepared) {
            Log.e(TAG, "camera thread didn't prepared");
            pendingStartPreview = true;
            return;
        }
        Log.d(TAG, "performCommand " + command);
        handler.sendEmptyMessage(command);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        isPrepared = true;
        handler = new Handler(getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_START:
                        startCamera();
                        break;
                    case MSG_STOP:
                        releaseCamera();
                        break;
                    case MSG_QUIT:
                        quitInternal();
                        break;
                    case MSG_CAPTURE:
                        takePicture();
                        break;
                    case MSG_SWITCH_FACING:
                        switchFacing();
                        break;
                }
            }

        };
        if (pendingStartPreview) {
            startCamera();
            pendingStartPreview = false;
        }
    }

    private void quitInternal() {
        releaseCamera();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            quitSafely();
        } else {
            quit();
        }
    }

    private void cameraStartPreview() {
        if (camera == null) {
            return;
        }
        if (holder == null) {
            Log.e(TAG, "holder not set,preview error");
            return;
        }

        try {
            camera.setPreviewDisplay(holder);
            if (setAfterOrientationInit) {
                setDisplayOrientation(mDisplayOrientation);
                setAfterOrientationInit = false;
            }
            camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "setSurfaceHolder", e);
        }
    }


    public void setSurfaceHolder(SurfaceHolder holder) {
        this.holder = holder;
    }

    public void switchFacing() {
        if (isCameraNotOpened()) {
            Log.e(TAG, "switchFacing camera is not opened");
            return;
        }

        if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        releaseCamera();
        startCamera();
    }

    private void startCamera() {
        if (!isOpenCamera.getAndSet(true)) {

            if (camera != null) {
                Log.d(TAG, "camera opened ,return;");
                isOpenCamera.set(false);
                return;
//                releaseCamera();
            }

            Log.d(TAG, "opening camera...");
            chooseCamera();

            if (mCameraId == INVALID_CAMERA_ID) {
                Log.e(TAG, "no camera choose ");
                isOpenCamera.set(false);
                return;
            }

            try {
                camera = Camera.open(mCameraId);
                if (holder == null) {
                    Log.e(TAG, "open camera error: surfaceholder is null");
                    isOpenCamera.set(false);
                    return;
                }
                camera.setPreviewDisplay(holder);
                mCameraParameters = camera.getParameters();
                adjustCameraParameters();
                camera.setDisplayOrientation(calcCameraRotation(mDisplayOrientation));
                cameraStartPreview();
                Log.d(TAG, "camera opened");
                if (callback != null)
                    callback.onCameraOpened();
            } catch (Exception e) {
                Log.e(TAG, "can't open camera");
            } finally {
                isOpenCamera.set(false);
            }
        }
    }

    private void releaseCamera() {
        handler.removeCallbacksAndMessages(null);
        if (camera != null) {
            Log.d(TAG,"releasing camera...");
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        Log.d(TAG, "camera closed");
        if (callback != null)
            callback.onCameraClosed();
    }

    private void adjustCameraParameters() {
        mCameraParameters.setRotation(calcCameraRotation(mDisplayOrientation));
        setAutoFocusInternal(mAutoFocus);
        setFlashInternal(mFlash);
        camera.setParameters(mCameraParameters);
    }

    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setFlashInternal(int flash) {
        if (isCameraNotOpened()) {
            mFlash = flash;
            return false;
        }
        List<String> modes = mCameraParameters.getSupportedFlashModes();
        String mode = FLASH_MODES.get(flash);
        if (modes != null && modes.contains(mode)) {
            mCameraParameters.setFlashMode(mode);
            mFlash = flash;
            return true;
        }
        String currentMode = FLASH_MODES.get(mFlash);
        if (modes == null || !modes.contains(currentMode)) {
            mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mFlash = Config.FLASH_OFF;
            return true;
        }
        return false;

    }

    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
        if (isCameraNotOpened()) {
            return false;
        }
        final List<String> modes = mCameraParameters.getSupportedFocusModes();
        if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            mCameraParameters.setFocusMode(modes.get(0));
        }
        return true;
    }

    private void chooseCamera() {
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == mFacing) {
                mCameraId = i;
                return;
            }
        }
        mCameraId = INVALID_CAMERA_ID;
    }

    public void stopCameraAndQuit() {
        Log.d(TAG, "stopCameraAndQuit");
        this.holder = null;
        this.callback = null;
        performCommand(MSG_QUIT);
    }

    void takePicture() {
        if (isCameraNotOpened()) {
            Log.e(TAG, "takePicture not inited.");
            return;
        }
        if (canAutoFocus()) {
            camera.cancelAutoFocus();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    takePictureInternal();
                }
            });
        } else {
            takePictureInternal();
        }
    }

    private void takePictureInternal() {
        if (!isPictureCaptureInProgress.getAndSet(true)) {
            camera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    isPictureCaptureInProgress.set(false);
                    Log.d(TAG, "takePictureInternal");
                    if (callback != null)
                        callback.onPictureTaken(data);
                    camera.cancelAutoFocus();
                    camera.startPreview();
                }
            });
        }
    }

    boolean canAutoFocus() {
        if (isCameraNotOpened()) {
            return mAutoFocus;
        }
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    private boolean isCameraNotOpened() {
        return camera == null;
    }

    public void startOpenCamera(SurfaceHolder holder) {
//        if (isCameraNotOpened()) {
            setSurfaceHolder(holder);
            performCommand(CameraHandler.MSG_START);
//        }
    }

    private int calcCameraRotation(int rotation) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Log.d(TAG, "calcCameraRotation CAMERA_FACING_FRONT");
            return (360 - (mCameraInfo.orientation + rotation) % 360) % 360;
        } else {  // back-facing
            Log.d(TAG, "calcCameraRotation CAMERA_FACING_BACK");
            return (mCameraInfo.orientation - rotation + 360) % 360;
        }
    }

    public boolean setDisplayOrientation(int displayOrientation) {

        if (mDisplayOrientation == displayOrientation) {
            return true;
        }
        mDisplayOrientation = displayOrientation;
        if (isCameraNotOpened()) {
            Log.d(TAG, "isCameraNotOpened");
            return false;
        }
        int cameraRotation = calcCameraRotation(displayOrientation);
        mCameraParameters.setRotation(cameraRotation);
        camera.setParameters(mCameraParameters);
        camera.setDisplayOrientation(cameraRotation);
        return true;
    }

    public void stopCamera() {
        if (isCameraNotOpened()) {
            return;
        }
        performCommand(MSG_STOP);
    }

    public interface Callback {
        void onCameraClosed();

        void onCameraOpened();

        void onPictureTaken(byte[] data);

    }
}
