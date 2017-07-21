package com.arover.camera.demo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arover.camera.CameraView;


/**
 * @author minstrel
 *         created at 21/07/2017 14:15
 */

class DemoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_TEXT = 2;
    private static final String TAG = "DemoAdapter";
    private final LayoutInflater mInflater;

    public DemoAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 1){
            return TYPE_CAMERA;
        }
        return TYPE_TEXT;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_CAMERA){
            Log.d(TAG, "onCreateViewHolder  camera");
            return new CameraViewHolder(mInflater.inflate(R.layout.li_camera, parent,false));
        }
        return new TextViewHolder(mInflater.inflate(R.layout.li_text, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof  CameraViewHolder ){
            ((CameraViewHolder)holder).cameraView.start();
            Log.d(TAG, "onBindViewHolder  camera");
        }else{
            ((TextViewHolder)holder).textView.setText(new String("label"+position));
        }
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if(holder instanceof  CameraViewHolder ){
            Log.d(TAG, "onViewRecycled camera stop");
            ((CameraViewHolder)holder).cameraView.stop();
        }
        super.onViewRecycled(holder);
    }


    static class CameraViewHolder extends RecyclerView.ViewHolder {
        private CameraView cameraView;
        public CameraViewHolder(View itemView) {
            super(itemView);
            cameraView = (CameraView) itemView;
        }
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public TextViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
