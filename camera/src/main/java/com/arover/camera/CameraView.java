package com.arover.camera;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author minstrel
 *         created at 19/07/2017 15:12
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, CameraHandler.Callback {

    private static final String TAG = "CameraView";
    private final SurfaceHolder holder;
    private final DisplayOrientationDetector mDisplayOrientationDetector;
    private CameraHandler cameraHandler;
    private Callback mCallback;

    public interface Callback{
        void onPictureTaken(byte[] data);
    }

    public CameraView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        cameraHandler = new CameraHandler(this);
        cameraHandler.start();
        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {
                cameraHandler.setDisplayOrientation(displayOrientation);
            }
        };
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }

    public void removeCallback(){
        mCallback = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cameraHandler.stopCameraAndQuit();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG,"surfaceCreated");
        cameraHandler.startOpenCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG,"surfaceChanged format="+format+",width="+width+",height="+height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG,"surfaceDestroyed");
        releaseCamera();
    }

    private void releaseCamera() {
        Log.d(TAG,"releaseCamera");
        holder.removeCallback(this);
        cameraHandler.stopCameraAndQuit();
    }

    @Override
    public void onCameraOpened() {
        Log.d(TAG,"onCameraOpened");
        cameraHandler.setSurfaceHolder(getHolder());
        cameraHandler.startCameraPreview();
    }

    @Override
    public void onCameraClosed() {
        Log.d(TAG,"onCameraClosed");
    }

    @Override
    public void onPictureTaken(byte[] data) {
        Log.d(TAG,"onPictureTaken");
        if(mCallback!=null){
            mCallback.onPictureTaken(data);
        }
    }

    public void switchFacing() {
        cameraHandler.switchFacing();
    }

    public void takePicture() {
        cameraHandler.takePicture();
    }
}
