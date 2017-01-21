package hitech.example.admin.volleyrequests;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static hitech.example.admin.volleyrequests.MainActivity.density;

/**
 * Created by Admin on 2016-12-16.
 */

public class CommentDialog extends Dialog {

    private static int id;
    private EditText et_comment;

    public CommentDialog(Context context, int imgId) {
        super(context);
        id = imgId;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.request_JSONgetComments(id, getContext(), new Utils.JSONCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("TAG", response.toString());
                try {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.comments_container);
                    int count = response.getJSONArray("data").length();
                    for (int i=0; i<=count; i++) {
                        TextView tv = new TextView(getContext());
                        //LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv.getLayoutParams();
                        //lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        tv.setPadding(20*density, 20*density, 2*density ,2*density );
                        tv.setGravity(Gravity.LEFT);
                        if ((i & 1) == 0) {
                            tv.setText(response.getJSONArray("data").getString(i));
                        } else {
                            tv.setText(response.getJSONArray("data").getString(i));
                            tv.setTextColor(Color.GRAY);
                            tv.setPadding(2, 2, 2, 2);
                        }

                        ll.addView(tv);
                    }

                    findViewById(R.id.comment_layout).setScrollY(200);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.comments);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        findViewById(R.id.button).setOnClickListener(onClickListener);
        et_comment = (EditText) findViewById(R.id.et_comment);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.request_commentImg(Adapter.bigImgId, et_comment.getText().toString(), ImgDataHolder.name, getContext(), new Utils.VolleyCallback() {
                @Override
                public void onSuccess(String response) {
                    dismiss();
                    //findViewById(R.id.comment_layout).scrollTo(0, -300);//findViewById(R.id.comment_layout).getBottom());
                }

                @Override
                public void onError() {

                }
            });
        }
    };
}
