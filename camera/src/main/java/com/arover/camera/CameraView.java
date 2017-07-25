package com.arover.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author minstrel
 *         created at 19/07/2017 15:12
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, CameraHandler.Callback {

    private static final String TAG = "CameraView";
    private SurfaceHolder holder;
    private DisplayOrientationDetector mDisplayOrientationDetector;
    private CameraHandler cameraHandler;
    private CameraHandler.CloseCallback mCloseCallback;
    private Callback mCallback;

    public void addCloseCallback(CameraHandler.CloseCallback closeCallback) {
        mCloseCallback = closeCallback;
    }

    public interface Callback{
        void onPictureTaken(byte[] data);

        void onCameraClose();

        void onCameraOpen();
    }

    public CameraView(Context context) {
        super(context);
        init(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void switchFacing() {
        cameraHandler.switchFacing();
    }

    public void takePicture() {
        cameraHandler.takePicture();
    }

    public void start(){
        Log.d(TAG,"start");
        cameraHandler.startOpenCamera(getHolder());
    }

    public void quit(){
        Log.d(TAG,"quit");
        holder.removeCallback(this);
        cameraHandler.stopCameraAndQuit();
    }

    public void stop(){
        Log.d(TAG,"stop");
        if(cameraHandler!=null)
            cameraHandler.stopCamera();
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }

    public void removeCallback(){
        mCallback = null;
        mCloseCallback = null;
    }
    public void removeCloseCallback(){
        mCloseCallback = null;
    }

    public void setAspectRatio(AspectRatio ratio) {
        cameraHandler.setAspectRation(ratio);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG,"surfaceCreated");
        cameraHandler.setSurfaceHolder(holder);
        cameraHandler.startOpenCamera(holder);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG,"surfaceChanged format="+format+",width="+width+",height="+height);
        cameraHandler.setSurfaceSize(width,height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG,"surfaceDestroyed camera stop");
        stop();
    }

    @Override
    public void onCameraOpened() {
        Log.d(TAG,"onCameraOpened");
        cameraHandler.setSurfaceHolder(getHolder());
        if(mCallback!=null) {
            mCallback.onCameraOpen();
        }
    }

    @Override
    public void onCameraClosed() {
        Log.d(TAG,"onCameraClosed");
        if(mCallback!=null) {
            mCallback.onCameraClose();
        }

        if(mCloseCallback!=null){
            mCloseCallback.onCameraClosed();
            mCloseCallback = null ;
        }
    }

    @Override
    public void onPictureTaken(byte[] data) {
        Log.d(TAG,"onPictureTaken");
        if(mCallback!=null) {
            mCallback.onPictureTaken(data);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility == GONE|| visibility == INVISIBLE){
            Log.d(TAG,"setVisibility not visible camera stop");
            stop();
        }else{
            Log.d(TAG,"setVisibility visible");
//            start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mDisplayOrientationDetector!=null) {
            mDisplayOrientationDetector.removeListener();
            mDisplayOrientationDetector = null;
        }
    }


    private void init(Context context) {
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
}
