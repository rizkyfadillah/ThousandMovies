package com.glkrenx.android.thousandmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.glkrenx.android.thousandmovies.api.APIServices;
import com.glkrenx.android.thousandmovies.db.MoviesDb;
import com.glkrenx.android.thousandmovies.db.MoviesDbHelper;
import com.glkrenx.android.thousandmovies.model.Movies;
import com.glkrenx.android.thousandmovies.model.Result;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    List<String> movies;
    SQLiteDatabase db;
    MoviesDbHelper dbHelper;
    GridView gridView;

    public MainActivityFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] listMovies = {};
        movies = new ArrayList<String>(Arrays.asList(listMovies));

        /*GridView gridView = (GridView)rootView.findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(getActivity(), movies));*/

        gridView = (GridView)rootView.findViewById(R.id.gridview);

        dbHelper = new MoviesDbHelper(getActivity());
        db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(
                MoviesDb.Movies.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        if(cursor.moveToFirst()){
            Log.d("teees", cursor.getString(cursor.getColumnIndex(MoviesDb.Movies.COLUMN_DESKRIPSI)));
        }

        if (cursor == null){
            loadAPI();
        }else {
            Toast.makeText(getActivity(), "DATABASE ADA", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void loadAPI(){
        Call<Movies> moviesCall = APIServices.apiServices.getMovies(1, "e7504adfe89a3fc5549f39c63c618e7c");

        moviesCall.enqueue(new Callback<Movies>() {
            @Override
            public void onResponse(Call<Movies> call, Response<Movies> response) {
                if (response.isSuccessful()) {
                    Movies query = response.body();
                    for (int i = 0; i < query.getResults().size(); i++) {
                        Result result = query.getResults().get(i);
                        String poster = "https://image.tmdb.org/t/p/w185" + result.getPosterPath();
                        String title = result.getTitle();
                        String tahun = result.getReleaseDate();
                        String deskripsi = result.getOverview();
                        String rating = String.valueOf(result.getVoteCount());
                        String genre = "daw";
                        movies.add(poster);
                        Bitmap bitmap = null;
                        try {
                            bitmap = Picasso.with(getActivity()).load(poster).get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String path = saveToInternalStorage(bitmap);
                        insertMovies(title, tahun, deskripsi, rating, genre, path);
                    }
                    gridView.setAdapter(new ImageAdapter(getActivity(), movies));
                    db.close();
                }
            }

            @Override
            public void onFailure(Call<Movies> call, Throwable t) {

            }
        });
    }

    private void insertMovies(String title, String tahun, String deskripsi, String rating, String genre, String path){
        ContentValues testValues = new ContentValues();
        testValues.put(MoviesDb.Movies.COLUMN_TITLE, title);
        testValues.put(MoviesDb.Movies.COLUMN_TAHUN, tahun);
        testValues.put(MoviesDb.Movies.COLUMN_DESKRIPSI, deskripsi);
        testValues.put(MoviesDb.Movies.COLUMN_RATING, rating);
        testValues.put(MoviesDb.Movies.COLUMN_GENRE, genre);
        testValues.put(MoviesDb.Movies.COLUMN_PATH, path);

        long locationRowId;
        locationRowId = db.insert(MoviesDb.Movies.TABLE_NAME, null, testValues);
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}
