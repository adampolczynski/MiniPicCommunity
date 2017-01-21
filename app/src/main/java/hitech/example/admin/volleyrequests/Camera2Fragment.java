package hitech.example.admin.volleyrequests;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Admin on 2016-12-11.
 */

// Huge thanks https://github.com/googlesamples/android-Camera2Basic

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Fragment extends Fragment implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static String TAG = "Cam2Fragment";
    private AutoFitTextureView texture_view_cam;
    private ImageButton but_take;
    private ImageButton but_cancel;
    private ImageButton but_edit;
    private ImgDataHolder imgDH;
    private Size previewSize;
    private static Size aspectRatio;
    private int sensorOrientation;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int DOUBLE_CLICK_DEFENDER = 1000;
    private long LAST_CLICK_TIME = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 200; // do api camera2
    private static CameraDevice CD; // rowniez do camera2, callback
    private ImageReader mImageReader;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CameraCaptureSession cameraCaptureSession;
    String mCameraId;
    File mFileMini = null;
    File mFile = null;
    Semaphore cameraOpenCloseLock = new Semaphore(1);


    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera2Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quit(); // a nie quitsafely

        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static Camera2Fragment newInstance() {
        return new Camera2Fragment();
    }

    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            System.out.println("capture started");

            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };
    final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile, mFileMini, getActivity()));
        }

    };

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraOpenCloseLock.release();
            CD = camera;
            System.out.println("kameralotwarta, tworze podglad" );
            createPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraOpenCloseLock.release();
            camera.close();
            CD = null;
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraOpenCloseLock.release();
            camera.close();
            CD = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };
    public void createPreview() {
        final CaptureRequest.Builder captureBuilder;

        try {
            Surface surface = new Surface(texture_view_cam.getSurfaceTexture());
            texture_view_cam.getSurfaceTexture().setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            captureBuilder = CD.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(surface);
            makeToast("PreviewSize: H:"+previewSize.getHeight()+"/ W:"+previewSize.getWidth());
            CD.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    try {
                        captureBuilder.set(CaptureRequest.JPEG_QUALITY, Byte.MAX_VALUE);
                        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90); // tu nalezalo wpiac 90 manualnie

                        session.setRepeatingRequest(captureBuilder.build(), captureCallback, texture_view_cam.getHandler());
                    } catch (CameraAccessException e) {
                        System.out.println("pipa"+e);
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    session.close();
                    System.out.println("conf failed");
                }
            }, null);

        } catch (CameraAccessException e) {
            System.out.println("CameraExc: "+e);
        }
    }
    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - LAST_CLICK_TIME < DOUBLE_CLICK_DEFENDER) {
            return;
        }
        LAST_CLICK_TIME = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.img_take:
                if (null != CD) {
                    takePicture();
                    but_take.setImageResource(android.R.drawable.ic_input_add);
                } else {
                    but_take.setVisibility(View.GONE);
                    getFragmentManager().popBackStack();
                }
                break;
            case R.id.img_cancel:
                getFragmentManager().popBackStack();
                break;
            case R.id.img_send:
                makeToast("Jeszcze nie mozna edytowac");
                break;
            default:
                break;
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cam_surface, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        texture_view_cam = (AutoFitTextureView) view.findViewById(R.id.textureView);
        but_edit = (ImageButton) view.findViewById(R.id.img_send);
        but_take = (ImageButton) view.findViewById(R.id.img_take);
        but_cancel = (ImageButton) view.findViewById(R.id.img_cancel);

        imgDH = null;
        mFile = new File(Environment.getExternalStorageDirectory(), "pic.jpg");
        mFileMini = new File (Environment.getExternalStorageDirectory(), "picmini.jpg");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        texture_view_cam.setOnClickListener(this);
        but_take.setOnClickListener(this);
        but_cancel.setOnClickListener(this);
        but_edit.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (texture_view_cam.isAvailable()) {
            System.out.println("onresume srfc valid");
        } else {
            startBackgroundThread();
            setUpCameraOutputs(texture_view_cam.getWidth(), texture_view_cam.getHeight());
            System.out.println("onresume srfc NOT valid");
            texture_view_cam.setSurfaceTextureListener(srfcTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    TextureView.SurfaceTextureListener srfcTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            cameraOpenCloseLock.release();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private void openCamera (int width, int height) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        setUpCameraOutputs(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time for waiting for camera passed");
            }
            manager.openCamera(mCameraId, stateCallback, mBackgroundHandler);
        } catch (CameraAccessException exc) {
            exc.printStackTrace();
        } catch (InterruptedException exc) {
            exc.printStackTrace();
        }
    }
    private void takePicture () {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        CaptureRequest.Builder requestBuilder = null;

        try {
            requestBuilder = CD.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        requestBuilder.addTarget(mImageReader.getSurface());
        // Focus
        requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        // Orientation
        //int rotation = windowManager.getDefaultDisplay().getRotation();

        requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
        requestBuilder.set(CaptureRequest.JPEG_QUALITY, (byte) 80);
        try {
            cameraCaptureSession.stopRepeating();
            // moze wrzucic do bezposrednio do ONCONFIGURED
            cameraCaptureSession.capture(requestBuilder.build(), captureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != cameraCaptureSession) {
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
            if (null != CD) {
                CD.close();
                CD = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }
    private void setUpCameraOutputs (int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            //for (String cameraId : manager.getCameraIdList()) {
            //}
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea()) ;
            Log.d(TAG, "mImageReaderSize: H:" + largest.getHeight() + " / W:" + largest.getWidth());
            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (sensorOrientation == 90 || sensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                        case Surface.ROTATION_270:
                            if (sensorOrientation == 0 || sensorOrientation == 180) {
                                swappedDimensions = true;
                            }
                            break;
                default:
                    Log.e(TAG, "Display rotation is invalid: "+displayRotation);
            }
            Log.e(TAG, "swappedDimen: "+swappedDimensions);
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;
            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }
            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }
            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }
            previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                texture_view_cam.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            } else {
                texture_view_cam.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            }
            mCameraId = cameraId;

        } catch (CameraAccessException exc) {
            exc.printStackTrace();
        }


    }
    private static Size chooseOptimalSize (Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option.getHeight() == option.getWidth() * h/w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        return bigEnough.get(0);
    }
    private void makeToast (final String content) {
        final Activity activity = getActivity();
        if (activity !=null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, content, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }
    class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;
        private final File mFileMini;
        private Context mContext;
        public ImageSaver(Image image, File file, File filemini, Context context) {
            mImage = image;
            mFile = file;
            mFileMini = filemini;
            mContext = context;
        }


        @Override
        public void run() {
            final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();

            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream outputMini = null;
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                outputMini = new FileOutputStream(mFileMini);
                output.write(bytes);

                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Matrix matrix = new Matrix(); // niepotrzebne!
                matrix.postRotate(90);              // obracamy obraz tworzac kolejny podajac za argument dana typu Matrix
                Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth()-bmp.getWidth()/5, bmp.getHeight()-bmp.getHeight()/5, matrix, true);
                Log.d(TAG, "rotatedBmpHeight: "+rotatedBmp.getHeight());
                Bitmap bmpMini = Bitmap.createScaledBitmap(rotatedBmp, rotatedBmp.getWidth()/4, rotatedBmp.getHeight()/4, true);

                // zapisujemy bitmapy
                bmpMini.compress(Bitmap.CompressFormat.JPEG, 100, outputMini);
                rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 75, output);
                entity.addPart("body", new FileBody(mFile));
                entity.addPart("bodymini", new FileBody(mFileMini));
                Utils.request_sendBmps(getActivity(), entity, new Utils.VolleyCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "onSendResponse:" +response);
                        if (response.length() < 3) {
                            makeToast("Zdjecie przeslano.");
                            but_cancel.performClick();
                        }
                    }

                    @Override
                    public void onError() {
                        makeToast("Problem z połączeniem");
                        but_cancel.performClick();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                        outputMini.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
