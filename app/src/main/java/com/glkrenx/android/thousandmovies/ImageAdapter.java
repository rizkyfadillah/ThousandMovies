package com.glkrenx.android.thousandmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by GLkrenx on 05/04/2016.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> urls = new ArrayList<String>();
    Cursor cursor;
    int flag;
    public static final int FLAG_URL = 0;
    public static final int FLAG_PATH = 1;

    public ImageAdapter(Context context, List<String> urls, int flag) {
        this.mContext = context;
        this.flag = flag;
        this.urls = urls;
        // menambahkan data movies dan membuat acak urutannya (random order)
        Collections.addAll(urls);
        Collections.shuffle(urls);

    }

    public int getCount() {
        return urls.size();
    }

    public String getItem(int position) {
        return urls.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;
        if(imageView == null){
            //g ada imageView jadi buat yang baru
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 110, 8, 8);

        }else {
            //jika ternyata sudah ada imageView, gunakan recycle view
            imageView = (ImageView) convertView;
        }

        String url = getItem(position);

        Log.d("url", url);

        // Trigger the download of the URL asynchronously into the image view.
        if(flag == FLAG_URL){
            Picasso.with(mContext)
                    .load(url)
                    .tag(mContext)
                    .into(imageView);
        } else {
            Picasso.with(mContext)
                    .load(new File(url))
                    .tag(mContext)
                    .into(imageView);
        }


        return imageView;
    }
}
