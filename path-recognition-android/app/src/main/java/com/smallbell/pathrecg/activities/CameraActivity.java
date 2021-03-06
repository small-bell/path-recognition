package com.smallbell.pathrecg.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.smallbell.pathrecg.R;
import com.smallbell.pathrecg.common.Config;
import com.smallbell.pathrecg.utils.ImageCompressUtils;
import com.smallbell.pathrecg.utils.OkHttpImageUploaderUtil;
import com.smallbell.pathrecg.views.AutoFitTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Response;

public class CameraActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // ?????????????????????????????????
    private FrameLayout rootLayout;
    // ??????????????????AutoFitTextureView??????,???????????????????????????
    private AutoFitTextureView textureView;
    // ?????????ID?????????0????????????????????????1????????????????????????
    private String mCameraId = "0";
    // ????????????????????????????????????
    private CameraDevice cameraDevice;
    // ????????????
    private Size previewSize;
    private CaptureRequest.Builder previewRequestBuilder;
    // ???????????????????????????????????????
    private CaptureRequest previewRequest;
    // ??????CameraCaptureSession????????????
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture
                , int width, int height) {
            // ???TextureView???????????????????????????
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture
                , int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        //  ????????????????????????????????????
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            CameraActivity.this.cameraDevice = cameraDevice;
            // ????????????
            createCameraPreviewSession();  // ???
        }

        // ???????????????????????????????????????
        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
        }

        // ?????????????????????????????????????????????
        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
            CameraActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rootLayout = findViewById(R.id.root);
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 0x123);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0x123 && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // ??????????????????????????????TextureView??????
            textureView = new AutoFitTextureView(CameraActivity.this, null);
            // ???TextureView?????????????????????
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
            rootLayout.addView(textureView);
            findViewById(R.id.capture).setOnClickListener(view -> captureStillPicture());
        }
    }

    private void captureStillPicture() {
        try {
            if (cameraDevice == null) {
                return;
            }
            // ?????????????????????CaptureRequest.Builder
            CaptureRequest.Builder captureRequestBuilder = cameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // ???imageReader???surface??????CaptureRequest.Builder?????????
            captureRequestBuilder.addTarget(imageReader.getSurface());
            // ????????????????????????
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // ????????????????????????
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // ??????????????????
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // ?????????????????????????????????????????????
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    ORIENTATIONS.get(rotation));
            // ??????????????????
            captureSession.stopRepeating();
            // ??????????????????
            captureSession.capture(captureRequestBuilder.build(),
                    new CameraCaptureSession.CaptureCallback()  // ???
                    {
                        // ??????????????????????????????
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                       @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            try {
                                // ????????????????????????
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                                // ????????????????????????
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                // ????????????????????????
                                captureSession.setRepeatingRequest(previewRequest, null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // ??????????????????????????????????????????????????????
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == previewSize) {
            return;
        }
        // ???????????????????????????
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        // ???????????????????????????
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        // ???????????????????????????
        else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    // ???????????????
    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // ??????????????????????????????????????????????????????
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // ???????????????
            manager.openCamera(mCameraId, stateCallback, null); // ???
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(texture);
            // ?????????????????????CaptureRequest.Builder
            previewRequestBuilder = cameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // ???textureView???surface??????CaptureRequest.Builder?????????
            previewRequestBuilder.addTarget(new Surface(texture));
            // ??????CameraCaptureSession?????????????????????????????????????????????????????????
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() // ???
                    {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // ??????????????????null?????????????????????
                            if (null == cameraDevice) {
                                return;
                            }
                            // ???????????????????????????????????????????????????
                            captureSession = cameraCaptureSession;
                            // ????????????????????????
                            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            // ????????????????????????
                            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                            // ????????????????????????
                            previewRequest = previewRequestBuilder.build();
                            try {
                                // ???????????????????????????????????????
                                captureSession.setRepeatingRequest(previewRequest, null, null);  // ???
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(CameraActivity.this, "???????????????",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // ??????????????????????????????
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            // ????????????????????????????????????
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.
                    SCALER_STREAM_CONFIGURATION_MAP);
            // ????????????????????????????????????
            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());
            // ????????????ImageReader?????????????????????????????????????????????
            imageReader = ImageReader.newInstance(largest.getWidth(),
                    largest.getHeight(), ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(reader -> {
                // ???????????????????????????????????????
                // ???????????????????????????
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                // ??????IO??????????????????????????????`
                File file = new File(getExternalFilesDir(null), "pic.jpg");

                ///////////////////////////////////////////////////////
                buffer.get(bytes);
                try (FileOutputStream output = new FileOutputStream(file)) {
                    output.write(bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    image.close();
                }
                ImageCompressUtils.compress(CameraActivity.this, file,
                        new ImageCompressUtils.ImageCompressCallBack() {
                            @Override
                            public void onSuccess(File file) {
                                super.onSuccess(file);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(CameraActivity.this, file.length(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        Response response =
                                                OkHttpImageUploaderUtil.updateHeadImg(Config.URL, file);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (response == null) {
                                                    Toast.makeText(CameraActivity.this, "????????????",
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    try {
                                                        String msg = response.body().string();
                                                        Toast.makeText(CameraActivity.this, "??????: "
                                                                + file + msg, Toast.LENGTH_SHORT).show();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }.start();
                            }
                        });



//                try {
//                    String msg = response.body().string();
//                    Toast.makeText(CameraActivity.this, "??????: "
//                            + file + msg, Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            }, null);
            // ???????????????????????????
            previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, largest);
            // ???????????????????????????????????????????????????TextureView??????????????????
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            } else {
                textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("???????????????");
        }
    }


    private static Size chooseOptimalSize(Size[] choices
            , int width, int height, Size aspectRatio) {
        // ????????????????????????????????????Surface????????????
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // ???????????????????????????????????????????????????????????????
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            System.out.println("???????????????????????????????????????");
            return choices[0];
        }
    }

    // ???Size?????????????????????Comparator
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // ?????????long????????????????????????
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}
