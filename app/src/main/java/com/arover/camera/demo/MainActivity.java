package com.arover.camera.demo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.arover.camera.CameraView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private FrameLayout previewSurface;

    private CameraView mCamearView;
    private RecyclerView mListView;
    private DemoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        previewSurface = (FrameLayout) findViewById(R.id.previewSurface);
        mListView = (RecyclerView) findViewById(R.id.list);
        mAdapter = new DemoAdapter(this);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.setAdapter(mAdapter);

//        previewSurface.addView(mCamearView);
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean granted) {
                        if(!granted){
                            finish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.switchFacingBtn:
//                mCamearView.switchFacing();
                break;
            case R.id.shutterBtn:
//                mCamearView.takePicture();
                break;
            case R.id.VisibleBtn:
                if(mListView.getVisibility() == View.VISIBLE){
                    mListView.setVisibility(View.GONE);
                }else{
                    mListView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
//        mCamearView.start();
    }

    @Override
    protected void onPause() {
//        mCamearView.stop();
        super.onPause();
    }


    @Override
    protected void onDestroy() {
//        mCamearView.quit();
        super.onDestroy();
    }
}
