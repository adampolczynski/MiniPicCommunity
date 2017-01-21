package hitech.example.admin.volleyrequests;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
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

/**
 * Created by Admin on 2016-12-13.
 */

public class CameraFragment extends Fragment implements View.OnClickListener {

    private AutoFitTextureView texture_view_cam;
    private ImageButton but_take;
    private ImageButton but_cancel;
    private ImageButton but_edit;

    private SurfaceTexture camSurface;
    private Camera.Size prevSize;
    final private String TAG = "CamFragment";
    private Camera camera;
    private ImgDataHolder imgDH;
    final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
    private static final int DOUBLE_CLICK_DEFENDER = 1000;
    private long LAST_CLICK_TIME = 0;
    File mFileMini = null;
    File mFile = null;

    public static CameraFragment newInstance() {
        return new CameraFragment();
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
            if (null == camera) {
                openCamera();
                camera.startPreview();
                return;
            }
            camera.startPreview();
        } else {
            texture_view_cam.setSurfaceTextureListener(surfaceTextureListener);
        }

    }
    @Override
    public void onPause() {
        super.onPause();
        camera.stopPreview();
    }

    @Override
    public void onStop() {
        super.onStop();
        camera.release();
        camera = null;
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - LAST_CLICK_TIME < DOUBLE_CLICK_DEFENDER) {
            return;
        }
        LAST_CLICK_TIME = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.img_take:
                if (null == imgDH) {
                    camera.takePicture(null, null, null, pictureCallback);
                    but_take.setImageResource(R.drawable.ok);
                } else {
                    sendPhoto();
                    but_take.setVisibility(View.GONE);
                    getFragmentManager().popBackStack();
                }
                break;
            case R.id.img_cancel:
                getFragmentManager().popBackStack();
                break;
            case R.id.img_send:
                makeToast(":)");
                break;
            default:
                break;
        }

    }
    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            camSurface = surface;
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (null == camera) {
                return false;
            }
            camera.release();
            camera = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
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
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            imgDH = new ImgDataHolder(data);
        }
    };
    private void openCamera () {
        if (null != camera) {
            Log.d("CAM", "cam not null");
            return;
        }
        camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        prevSize = params.getPreviewSize();
        if (Build.VERSION.SDK_INT != 16) { // 16  =  JELLYBEAN
            params.setJpegQuality(100);
            params.set("orientation", "portrait");
            params.set("rotation", 90);

        }
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setPreviewSize(prevSize.width, prevSize.height);
        params.setPictureSize(prevSize.width, prevSize.height);
        camera.setDisplayOrientation(90);
        camera.setParameters(params);
        try {
            camera.setPreviewTexture(camSurface);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }
    private void sendPhoto () {

        byte[] data = imgDH.getDataStored();
        FileOutputStream os = null;
        FileOutputStream osMini = null;
        if (mFile.exists()) {
            mFile.delete();
        }
        if (mFileMini.exists()) {
            mFileMini.delete();
        }
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        Log.d(TAG, "takenBmpHeight: "+bmp.getHeight());

        try {
            mFile.createNewFile();
            mFileMini.createNewFile();
            os = new FileOutputStream(mFile);
            osMini = new FileOutputStream(mFileMini);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix matrix = new Matrix(); // niepotrzebne!
        matrix.postRotate(0);              // obracamy obraz tworzac kolejny podajac za argument dana typu Matrix
        Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth()-bmp.getWidth()/5, bmp.getHeight()-bmp.getHeight()/5, matrix, true);
        Log.d(TAG, "rotatedBmpHeight: "+rotatedBmp.getHeight());
        Bitmap bmpMini = Bitmap.createScaledBitmap(rotatedBmp, rotatedBmp.getWidth()/4, rotatedBmp.getHeight()/4, true);

        // zapisujemy bitmapy
        bmpMini.compress(Bitmap.CompressFormat.JPEG, 100, osMini);
        rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 75, os);
        entity.addPart("body", new FileBody(mFile));
        entity.addPart("bodymini", new FileBody(mFileMini));
        Utils.request_sendBmps(getActivity(), entity, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "onSendResponse:" +response);
                if (response.length() < 3) {
                    makeToast(getString(R.string.photosent));
                    but_cancel.performClick();
                }
            }

            @Override
            public void onError() {
                makeToast(getString(R.string.connproblem));
                but_cancel.performClick();
            }
        });
    }
}
