package hitech.example.admin.volleyrequests;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static hitech.example.admin.volleyrequests.MainActivity.density;

/**
 * Created by Admin on 2016-11-14.
 */

public class BigImage extends Dialog {

    RotateAnimation anim;
    Animation anim2;
    Animation anim3;
    ImageView imgV;
    ImageButton ib_handDown;
    ImageButton ib_handUp;
    ImageButton ib_comment;
    TextView tv_rating;
    boolean mShort;
    int screenWidth;
    int screenHeight;

    public BigImage(Context context, boolean short_long) {
        super(context);
        mShort = short_long;
    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.big_img_layout).setOnClickListener(onClickListener);
        //ib_comment.setOnClickListener(onClickListener);
        ib_handDown.setOnClickListener(onClickListener);
        ib_handUp.setOnClickListener(onClickListener);
        imgV.setOnClickListener(onClickListener);
        imgV.startAnimation(anim);
        imgV.setOnTouchListener(onSwipeListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.big_image);
        Display display = null;
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); // to ustrojstwo uratowwalo czern
        display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        imgV = (ImageView) findViewById(R.id.imgV);
        tv_rating = (TextView) findViewById(R.id.rating);
        //ib_comment = (ImageButton) findViewById(R.id.comment);
        ib_handDown = (ImageButton) findViewById(R.id.hand_down);
        ib_handUp = (ImageButton) findViewById(R.id.hand_up);
        setAnims ();
    }
    OnSwipeTouchListener onSwipeListener = new OnSwipeTouchListener(getContext()) {
        @Override
        public void onSwipeBottom() {
            dismiss();
            super.onSwipeBottom();
        }
        @Override
        public void onSwipeTop() {
            dismiss();
            super.onSwipeBottom();
        }
        @Override
        public void onSwipeLeft() {
            switchImage(-1, true, 0);
        }

        @Override
        public void onSwipeRight() {
            switchImage(1, false, 0);
            super.onSwipeRight();
        }

    };
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(ib_handUp)) {
                rateImg(1, "");
                return;
            }
            if (v.equals(ib_handDown)) {
                rateImg(-1, "");
                return;
            }
            if (v.equals(ib_comment)) {
                rateImg(0, "comment");
                CommentDialog cDialog = new CommentDialog(getContext(), Adapter.bigImgId);
                cDialog.show();
                return;
            }
            dismiss();
            return;
        }
    };
    private void setAnims () {
        // animacja zmiany obrazka na nastepny
        anim2 = new TranslateAnimation(0f, 400f, 0f, 0f);
        anim2.setDuration(200);
        anim2.setAnimationListener(animationListener);
        // animacja zmiany na poprzedni
        anim3 = new TranslateAnimation(0f, -400f, 0f, 0f);
        anim3.setDuration(200);
        anim3.setAnimationListener(animationListener);
        // animacja krazka wokol wlasnego centrum
        anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(1000);
        anim.setRepeatCount(Animation.INFINITE);
    }
    private void rateImg (final int rate, String comment) {
        Utils.request_rateImg(Adapter.bigImgId, rate, comment, ImgDataHolder.name, getContext(), new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String response) {
                if (response.length() > 8) {

                } else {
                    switchImage(0, false, Adapter.bigImgId);
                }
            }

            @Override
            public void onError() {
                Toast.makeText(getContext(), "Zrobił sie problem :< !", Toast.LENGTH_SHORT).show();
            }
        });
    }
Animation.AnimationListener animationListener = new Animation.AnimationListener() {
    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        imgV.setImageBitmap(null);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
};
    public void switchImage (int downUp, boolean animInOut, final int imgId) {
        Animation a;
        if (animInOut) {
            a = anim3;
        } else {
            a = anim2;
        }
        if (imgId == 0) {
            //ib_comment.setVisibility(View.GONE);
            ib_handDown.setVisibility(View.GONE);
            ib_handUp.setVisibility(View.GONE);
            tv_rating.setVisibility(View.GONE);
            imgV.startAnimation(a);

        }
        final int id = Adapter.bigImgId + downUp;
        // nastepujaca instrukcja sprawdza czy nie wykraczamy poza liste
        if (id == (Adapter.actualImgsIDs.get(Adapter.actualImgsIDs.size()-1)) || id == Adapter.actualImgsIDs.get(0)+1) {
            dismiss();
            return;
        }
        Utils.request_JSONgetBigImg(id, getContext(), new Utils.JSONCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d("TAG", response.toString());
                    String imgBase64 = response.getJSONArray("data").getString(0);
                    int overallRate = Integer.parseInt(response.getJSONArray("data").getString(1));
                    int userRate = Integer.parseInt(response.getJSONArray("data").getString(2));
                    byte[] b = Base64.decode(imgBase64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                    imgV.clearAnimation();
                    if (null == bmp) {
                        Point size = new Point();
                        size.x = 150 * density;
                        size.y = 150 * density;
                        setUpBigImageSize(size, false);
                        imgV.setImageResource(R.drawable.anger_red);
                        Adapter.bigImgId = id;
                        return;
                    }
                    Point size = new Point();
                    size.x = bmp.getWidth();
                    size.y = bmp.getHeight();
                    setUpBigImageSize(size, true);
                    setUpIconColors(userRate);
                    tv_rating.setText(Integer.toString(overallRate));
                    imgV.setImageBitmap(bmp);
                    //ib_comment.setVisibility(View.VISIBLE);
                    ib_handDown.setVisibility(View.VISIBLE);
                    ib_handUp.setVisibility(View.VISIBLE);
                    tv_rating.setVisibility(View.VISIBLE);
                    Adapter.bigImgId = id;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void setUpIconColors (int rate) {
        switch (rate) {
            case -1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ib_handDown.setImageResource(R.drawable.handdownred);
                    ib_handUp.setImageResource(R.drawable.handuporion);
                }

                break;
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ib_handUp.setImageResource(R.drawable.handupgreen);
                    ib_handDown.setImageResource(R.drawable.handdownorion);
                }
                break;
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ib_handUp.setImageResource(R.drawable.handuporion);
                    ib_handDown.setImageResource(R.drawable.handdownorion);
                }
                break;
        }
    }
    private void setUpBigImageSize (Point imgSize, boolean isScalable) {
        if (!isScalable) {
            ViewGroup.LayoutParams layParams = imgV.getLayoutParams();
            layParams.width = imgSize.x;
            layParams.height = imgSize.y;
            imgV.setLayoutParams(layParams);
            return;
        }
        ViewGroup.LayoutParams layParams = imgV.getLayoutParams();
        if (imgSize.x > screenWidth) { // jesli szerokosc obrazka jest wieksza nizeli ekran
            layParams.width = screenWidth;
            layParams.height = (screenWidth * imgSize.y) / imgSize.x;
        } else if (imgSize.y > screenHeight) { // jesli wysokosc obrazka jest wieksza nizeli nasz ekran
            layParams.width = (screenHeight * imgSize.x) / imgSize.y;
            layParams.height = screenHeight;
        } else { // jesli obraz jest mniejszy nizeli ekran
            if (imgSize.y > imgSize.x) {
                layParams.width = screenWidth;
                layParams.height = (screenWidth * imgSize.y) / imgSize.x;
            } else {
                layParams.height = (screenHeight *imgSize.x) / imgSize.y;
                layParams.width = screenWidth;
            }
        }
        //Toast.makeText(getContext(), "X: "+imgSize.x+", Y: " +imgSize.y +"\r\n"+"Chgd: X: " +layParams.width + ", Y:" + layParams.height, Toast.LENGTH_SHORT).show();
        imgV.setPadding(25*density, 35*density, 25*density, 0);
        imgV.setLayoutParams(layParams);
    }
}
