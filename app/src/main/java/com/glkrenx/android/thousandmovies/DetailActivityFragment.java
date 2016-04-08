package com.glkrenx.android.thousandmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    String title, tahun, deskripsi, path, rating;
    int flag;

    TextView tahunTextView, titleTextView, deskripsiTextView, ratingTextView;
    ImageView poster;

    View rootview;

    public DetailActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_detail, container, false);

        deskripsiTextView = (TextView) rootview.findViewById(R.id.deskripsiView);
        titleTextView = (TextView) rootview.findViewById(R.id.titleView);
        tahunTextView = (TextView) rootview.findViewById(R.id.tahunView);
        ratingTextView = (TextView) rootview.findViewById(R.id.ratingView);

        poster = (ImageView) rootview.findViewById(R.id.imageView);

        deskripsiTextView.setText(deskripsi);
        titleTextView.setText(title);
        tahunTextView.setText(tahun);
        ratingTextView.setText(rating);
        //if(flag == MainActivityFragment.FLAG_DATABASE)
            //Picasso.with(getContext()).load(new File(path)).into(poster);
        //else
            Picasso.with(getContext()).load(path).into(poster);

        return rootview;
    }

    public void setResult(String title, String tahun, String deskripsi, String rating, String path, int flag) {
        this.title = title;
        this.tahun = tahun;
        this.deskripsi = deskripsi;
        this.rating = rating;
        this.path = path;
        this.flag = flag;
    }
}
