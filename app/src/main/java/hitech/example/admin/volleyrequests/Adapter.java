package hitech.example.admin.volleyrequests;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class Adapter extends BaseAdapter {

    public static ArrayList<Integer> actualImgsIDs; // ile to to mamy elementow
    private Context mContext;
    private ArrayList<Bitmap> mBmpList;
    public static int bigImgId = 0; // id obrazka, ktory jest aktualnie wyswietlany

    public Adapter (ArrayList<Bitmap> bmpList, Context context) {
        mBmpList = bmpList;
        mContext = context;
    }
    static class ViewHolder {
        ImageView iv;
    }

    @Override
    public int getCount() {
        return mBmpList.size()-1;
    }

    @Override
    public Object getItem(int position) {
        return mBmpList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_img_grid, parent, false);
            vh = new ViewHolder();
            vh.iv = (ImageView) convertView.findViewById(R.id.grid_image);
            convertView.setTag(vh);

        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.iv.setImageBitmap(mBmpList.get(position));
        if (position < actualImgsIDs.size()) {
            vh.iv.setId(actualImgsIDs.get(position));
        }

        vh.iv.setOnClickListener(onClickListener);
        return convertView;
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final BigImage bI = new BigImage(mContext, false);
            final String naszeID = String.valueOf(v.getId());
            bigImgId = Integer.parseInt(naszeID);
            bI.show();
            bI.switchImage(0, false, Integer.parseInt(naszeID));
        }
    };
}
