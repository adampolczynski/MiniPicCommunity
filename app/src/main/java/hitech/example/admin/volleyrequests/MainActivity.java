package hitech.example.admin.volleyrequests;

import android.app.Fragment;
import android.content.DialogInterface;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;


public class MainActivity extends AppCompatActivity {

    private static final int LOLLIPOP = Build.VERSION_CODES.LOLLIPOP;

    static boolean WELCOME_VIEWED = false;
    private static final int DOUBLE_CLICK_DEFENDER = 1000;
    private long LAST_CLICK_TIME = 0;
    public static int density;

    private SeekBar seekBar;
    private GridView gridView;
    private ImageView progressB;
    private ImageButton nextBut;
    private ImageButton refresh;

    @Override
    protected void onStart() {
        super.onStart();
        nextBut.setOnClickListener(cameraButtonListener);
        refresh.setOnClickListener(onRefreshClick);
        new AsyncTask(getWindow().getContext(), progressB, gridView).execute();

        if (!WELCOME_VIEWED) {
            WelcomeDialog welcomeDialog = new WelcomeDialog(getWindow().getContext());
            if (welcomeDialog.isShowing()) {
                return;
            }
            welcomeDialog.show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gridView = (GridView) findViewById(R.id.gridview);
        nextBut = (ImageButton) findViewById(R.id.addNext);
        refresh = (ImageButton) findViewById(R.id.refresh);
        progressB = (ImageView) findViewById(R.id.progressBar);
        density = (int) getResources().getDisplayMetrics().density;
        seekBar = (SeekBar) findViewById(R.id.seekBar2);
        seekBar.setMax(5);
        seekBar.setProgress(2);
        //seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        ImgDataHolder.name = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    View.OnClickListener cameraButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - LAST_CLICK_TIME < DOUBLE_CLICK_DEFENDER) {
                return;
            }
            LAST_CLICK_TIME = SystemClock.elapsedRealtime();
            if (Build.VERSION.SDK_INT >= LOLLIPOP) {
                if (null == getFragmentManager().findFragmentById(R.id.main_layout)) {
                    android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.animator.animator_in, R.animator.animator_in, R.animator.animator_out, R.animator.animator_out);
                    ft.replace(R.id.main_layout, Camera2Fragment.newInstance());
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    return;
                }
            } else {
                if (null == getFragmentManager().findFragmentById(R.id.main_layout)) {
                    android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.animator.animator_in, R.animator.animator_in, R.animator.animator_out, R.animator.animator_out);
                    ft.replace(R.id.main_layout, CameraFragment.newInstance());
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    return;
                }
            }
            }

    };
    View.OnClickListener onRefreshClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - LAST_CLICK_TIME < DOUBLE_CLICK_DEFENDER) {
                return;
            }
            LAST_CLICK_TIME = SystemClock.elapsedRealtime();
            new AsyncTask(getWindow().getContext(), progressB, gridView).execute();
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (action == KeyEvent.ACTION_DOWN) {
                    Fragment fr = getFragmentManager().findFragmentById(R.id.main_layout);
                    if (fr == null) {
                        Log.d("KEY", "Fragment null, exiting");
                        finish();
                    } else {
                        getFragmentManager().popBackStack();
                    }
                    Log.d("KEY", "BACK PRESSED");
                }
                return false;

            default:
                return super.dispatchKeyEvent(event);
        }
    }

}
