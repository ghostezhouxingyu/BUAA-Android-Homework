package com.example.evideo;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.example.evideo.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.example.evideo.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.example.evideo.utils.Utils.getOutputMediaFile;
import static com.example.evideo.utils.Utils.getOutputMediaString;

public class RecordVideoActicity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;
    private boolean isZoomIn = true;

    private int rotationDegree = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_record_video);

        mSurfaceView = findViewById(R.id.img);
        //to.do 给SurfaceHolder添加Callback
        mCamera = getCamera(CAMERA_TYPE);
        final SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        });

        findViewById(R.id.btn_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to.do 拍一张照片
                mCamera.takePicture(null, null, mPicture);
                Toast.makeText(getApplicationContext(), "picture has been saved", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btn_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to.do 录制，第一次点击是start，第二次点击是stop
                if (isRecording) {
                    //to.do 停止录制
                    isRecording = false;
                    RecordVideoActicity.this.releaseMediaRecorder();
                    Toast.makeText(getApplicationContext(), "video has been saved", Toast.LENGTH_LONG).show();
                } else {
                    //to.do 录制
                    isRecording = true;
                    RecordVideoActicity.this.prepareVideoRecorder();
                    try {
                        mMediaRecorder.prepare();
                        mMediaRecorder.start();
                    } catch (Exception e) {
                        mMediaRecorder.release();
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to.do 切换前后摄像头
                if (CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    mCamera = RecordVideoActicity.this.getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                else
                    mCamera = RecordVideoActicity.this.getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                RecordVideoActicity.this.startPreview(mSurfaceHolder);
            }
        });

        findViewById(R.id.btn_zoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo 调焦，需要判断手机是否支持
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.isZoomSupported()) {
                    int maxZoom = parameters.getMaxZoom();
                    int zoom = parameters.getZoom();
                    if (isZoomIn) {
                        if (zoom < maxZoom)
                            zoom++;
                        else
                            isZoomIn = false;
                    } else {
                        if (zoom > 0)
                            zoom--;
                        else
                            isZoomIn = true;
                    }
                    parameters.setZoom(zoom);
                    mCamera.setParameters(parameters);
                }
            }
        });
    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        Camera.Parameters params = cam.getParameters();
        //List<Camera.Size> prevSizes = params.getSupportedPreviewSizes();
        //size = getOptimalPreviewSize(prevSizes, )
//        cam.autoFocus(new Camera.AutoFocusCallback() {
//            @Override
//            public void onAutoFocus(boolean b, Camera camera) {
//                if(b){
//                    camera.setOneShotPreviewCallback(null);
//                }
//            }
//        });
        List<String> focusModes = params.getSupportedFocusModes();
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        rotationDegree = getCameraDisplayOrientation(CAMERA_TYPE);
        cam.setDisplayOrientation(rotationDegree);
        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //to.do 释放camera资源
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        //to.do 开始预览
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //to.do 准备MediaRecorder
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO));
        }else{
            Log.d("fordebug", "prepareVideoRecorder: " + getOutputMediaString(MEDIA_TYPE_VIDEO));
            mMediaRecorder.setOutputFile(getOutputMediaString(MEDIA_TYPE_VIDEO));
        }

        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());

        mMediaRecorder.setOrientationHint(rotationDegree);
        return true;
    }


    private void releaseMediaRecorder() {
        //to.do 释放MediaRecorder
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                Log.d("mPicture", "Error accessing file: " + e.getMessage());
            }

            try {
                ExifInterface srcExif = new ExifInterface(pictureFile.getAbsolutePath());
                switch (rotationDegree) {
                    case DEGREE_90:
                        srcExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                        break;
                    case DEGREE_180:
                        srcExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                        break;
                    case DEGREE_270:
                        srcExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                        break;
                    default:
                        break;
                }
                srcExif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCamera.startPreview();
        }
    };


}
