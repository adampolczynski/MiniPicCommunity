package hitech.example.admin.volleyrequests;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Admin on 2016-11-14.
 */

public class WelcomeDialog extends Dialog {

    private boolean CLICKED_ONCE = false;
    TextView tv;
    TextView tv_two;
    ImageView iv_logo;
    CheckBox check_welcome;
    public WelcomeDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_welcome);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); // to ustrojstwo uratowwalo czern

        ColorDrawable bgDrawable = new ColorDrawable(Color.BLACK);
        bgDrawable.setAlpha(150);
        this.getWindow().setBackgroundDrawable(bgDrawable);
        tv = (TextView) findViewById(R.id.welcome_text);
        tv_two = (TextView) findViewById(R.id.welcome_text_two);
        iv_logo = (ImageView) findViewById(R.id.welcome_logo);
        check_welcome = (CheckBox) findViewById(R.id.welcome_check);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (CLICKED_ONCE) {
                CLICKED_ONCE = false;
                dismiss();
            } else {
                tv.setText(R.string.more_info);
                tv_two.setText(R.string.more_info_two);
                check_welcome.setVisibility(View.VISIBLE);
                CLICKED_ONCE = true;
                return;
            }
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        check_welcome.setOnCheckedChangeListener(onCheckedChangeListener);
        this.setCanceledOnTouchOutside(false);
        findViewById(R.id.layout).setOnClickListener(onClickListener);
    }

    CheckBox.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                MainActivity.WELCOME_VIEWED = true;
            }
        }
    };

}
