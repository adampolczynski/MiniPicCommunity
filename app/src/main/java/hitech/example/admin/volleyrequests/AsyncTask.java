package hitech.example.admin.volleyrequests;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Admin on 2016-11-17.
 */
// sciaga dla nas obrazy przy pomocy GetGrid.java, nastepnie sciaga bitmapy przekazujac do adaptera jako liste bitmap
public class AsyncTask extends android.os.AsyncTask<Void, Integer, ArrayList<Bitmap>> {

    Context mContext;
    Bitmap bmp;
    ArrayList<Bitmap> bmpList = new ArrayList<Bitmap>(); // zawsze pamietaj o inicjalizacji zmiennych pało
    ArrayList<Integer> bmpIds = new ArrayList<Integer>();
    GridView mGridView;
    ImageView mProgressBar;

    public AsyncTask (Context context, ImageView progressBar, GridView gridView) {
        mContext = context;
        mProgressBar = progressBar;
        mGridView = gridView;
    }
    @Override
    protected ArrayList<Bitmap> doInBackground(Void... params) {
        final Activity activity = (Activity) mContext;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(500);
                anim.setRepeatCount(Animation.INFINITE);
                mProgressBar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.twist_white));
                mProgressBar.startAnimation(anim);
            }
        });

        Utils.request_getBinImgs(mContext, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String response) {
                String[] base64StringSeparate = response.split(" ");
                for (int i=0; i<base64StringSeparate.length-1; i++) {
                    if ((i&1) == 0) {
                        byte[] bbbb = Base64.decode(base64StringSeparate[i], Base64.DEFAULT);

                        bmp = BitmapFactory.decodeByteArray(bbbb, 0 , bbbb.length);
                        bmpList.add(bmp);
                    } else {
                        bmpIds.add(Integer.parseInt(base64StringSeparate[i]));
                    }
                }
                Adapter.actualImgsIDs = bmpIds;
                publishProgress(2);
                onPostExecute(bmpList); // bez try ponizej callbacku to jedyna opcja na powodzenie poki co
            }

            @Override
            public void onError() {
                final Activity activity = (Activity) mContext;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishProgress(1);
                        Toast.makeText(activity, activity.getString(R.string.connproblem), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return bmpList; // zwraca pusta liste przed response
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> aVoid) {
//        delegate.processFin(aVoid);
        if (bmpList.size() < 4) { // bo dwa razy wykonujemy onPost :<
            return;
        }
        Adapter adapter = new Adapter(aVoid, mContext);
        mGridView.setAdapter(adapter);
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (values[0] == 2) {
            mProgressBar.clearAnimation();
            mProgressBar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.laugh_green));
        } else if (values[0] == 1) {
            mProgressBar.clearAnimation();
            mProgressBar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.sad_red));
        } else {
            mProgressBar.clearAnimation();
            mProgressBar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.anger_white));
        }


    }
}
