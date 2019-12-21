package com.trackkers.tmark.helper;


import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class ImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    Camera mCamera;
    SurfaceHolder mHolder;
    Context mContext;

    public ImageSurfaceView(Context context, Camera camera) {
        super(context);
        mContext=context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e("SurfaceView", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            if (holder.getSurface()==null){
                return;
            }

            try {
                mCamera.stopPreview();
            } catch (Exception e){
                Log.e("SurfaceStopPreview", "Error setting camera preview: " + e.getMessage());
            }

        try {

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e("SurfaceViewChanged", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
