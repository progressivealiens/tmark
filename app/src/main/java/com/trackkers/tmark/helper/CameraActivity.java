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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;
    @BindView(R.id.fab_take_photo)
    FloatingActionButton fabTakePhoto;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public ImageSurfaceView mImageSurfaceView;
    public Camera camera=null;
    public Camera mCamera = null;

    int numberOfCameras = 0, camId = 0;
    String timeStamp = "";
    File file = null;
    Display display;
    private boolean safeToTakePicture = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        initialize();

        fabTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (safeToTakePicture) {
                    camera.takePicture(null, null, pictureCallback);
                    safeToTakePicture = false;
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
        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        try{
            camera = checkDeviceCamera();
            mImageSurfaceView = new ImageSurfaceView(CameraActivity.this, camera);
            safeToTakePicture = true;

            if (display.getRotation() == Surface.ROTATION_0) {
                camera.setDisplayOrientation(90);
            }

        }catch(RuntimeException e){
            e.printStackTrace();

            camera = checkDeviceCamera();
            mImageSurfaceView = new ImageSurfaceView(CameraActivity.this, camera);
            safeToTakePicture = true;

            if (display.getRotation() == Surface.ROTATION_0) {
                camera.setDisplayOrientation(90);
            }

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
            camId = 0;
            try {
                mCamera = Camera.open(0);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        } else if (numberOfCameras == 2) {
            camId = 1;
            try {
                mCamera = Camera.open(1);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }else{
            camId = 1;
            try {
                mCamera = Camera.open(1);
            } catch (RuntimeException e) {
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
                safeToTakePicture = true;
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

                safeToTakePicture = true;

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }finally {
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
        Log.e("cameraDestroy", "cameraDestroy");
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
    }
}
