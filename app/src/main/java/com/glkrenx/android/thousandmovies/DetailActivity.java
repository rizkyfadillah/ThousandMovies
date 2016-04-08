package com.glkrenx.android.thousandmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        DetailActivityFragment fragment = new DetailActivityFragment();

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String tahun = intent.getStringExtra("tahun");
        String deskripsi = intent.getStringExtra("deskripsi");
        String rating = intent.getStringExtra("rating");
        String path = intent.getStringExtra("path");
        int flag = intent.getIntExtra("flag", 0);

        fragment.setResult(title, tahun, deskripsi, rating, path, flag);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.movie_detail_container, fragment)
                .commit();
    }

}
