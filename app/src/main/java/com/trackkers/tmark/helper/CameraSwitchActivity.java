package com.trackkers.tmark.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraSwitchActivity extends AppCompatActivity {

    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;
    @BindView(R.id.fab_take_photo)
    FloatingActionButton fabTakePhoto;
    @BindView(R.id.fab_switch_camera)
    FloatingActionButton fabSwitchCamera;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ImageSurfaceView mImageSurfaceView;
    Display display;
    private Camera camera;
    public static Camera mCamera = null;
    int numberOfCameras = 0, camId = 0;
    String timeStamp = "";
    File file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_switch);
        ButterKnife.bind(this);

        initialize();

        fabTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, pictureCallback);
            }
        });

        fabSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                numberOfCameras = Camera.getNumberOfCameras();

                if (numberOfCameras == 2) {
                    if (camId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        camId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        Log.e("camIdBack", camId + "");
                    } else {
                        camId = Camera.CameraInfo.CAMERA_FACING_BACK;
                        Log.e("camIdFront", camId + "");
                    }

                    try {

                        if (camera != null) {
                            camera.stopPreview();
                            camera.release();
                            Log.e("closingPreview", "Closing Camera Preview");
                        }

                        camera = Camera.open(camId);
                        mImageSurfaceView = new ImageSurfaceView(CameraSwitchActivity.this, camera);
                        if (display.getRotation() == Surface.ROTATION_0) {
                            camera.setDisplayOrientation(90);
                        }
                        frameLayout.addView(mImageSurfaceView);
                    } catch (Exception e) {
                        Log.e("ErrorCamers", e.getMessage());
                    }

                } else {
                    Toast.makeText(CameraSwitchActivity.this, "Your phone doesn't have front camera", Toast.LENGTH_SHORT).show();
                }
            }
        });


        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void initialize() {

        setSupportActionBar(toolbar);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText("Hows your day today");

        timeStamp = Utils.currentTimeStamp();
        file = new File(getExternalFilesDir(null), timeStamp + ".png");

        camera = checkDeviceCamera();

        mImageSurfaceView = new ImageSurfaceView(CameraSwitchActivity.this, camera);
        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getRotation() == Surface.ROTATION_0) {
            camera.setDisplayOrientation(90);
        }

        frameLayout.addView(mImageSurfaceView);

    }

    private Camera checkDeviceCamera() {

        numberOfCameras = Camera.getNumberOfCameras();

        if (numberOfCameras == 0) {
            Toast.makeText(this, "Your phone doesn't have any camera", Toast.LENGTH_LONG).show();
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();

        } else if (numberOfCameras == 1) {
            try {
                camId = Camera.CameraInfo.CAMERA_FACING_BACK;
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (numberOfCameras == 2) {
            try {
                camId = Camera.CameraInfo.CAMERA_FACING_BACK;
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mCamera;
    }

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bitmap == null) {
                return;
            }

            Matrix matrix = new Matrix();
            if (camId == 1) {
                matrix.postRotate(270);
            } else {
                matrix.postRotate(270 - 180);
            }


            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels * 70 / 100;
            int width = displayMetrics.widthPixels * 70 / 100;
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, height, width, true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);


            ByteArrayOutputStream out = new ByteArrayOutputStream();
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
            try {
                save(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void save(ByteArrayOutputStream bytes) throws IOException {
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(bytes.toByteArray());

                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", file.getPath());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

            } finally {
                if (null != output) {
                    output.close();
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("cameraDestroy", "CameraSwitchDestroy");
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
    }


}