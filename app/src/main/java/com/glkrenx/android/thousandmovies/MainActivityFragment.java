package com.glkrenx.android.thousandmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import com.squareup.picasso.Target;

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
    GridView gridView;
    String [] title, poster, tahun, deskripsi, rating, genre;
    String path;
    MoviesDbHelper dbHelper;
    SQLiteDatabase db;

    public MainActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] listMovies = {};
        movies = new ArrayList<String>(Arrays.asList(listMovies));

        dbHelper = new MoviesDbHelper(getActivity());
        db = dbHelper.getWritableDatabase();

        gridView = (GridView) rootView.findViewById(R.id.gridview);

        poster = new String[30];
        title = new String[30];
        genre = new String[30];
        tahun = new String[30];
        deskripsi = new String[30];
        rating = new String[30];

        Cursor cursor = db.query(
                MoviesDb.Movies.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        //asdf

        if(!cursor.moveToFirst()) {
            loadAPI();
            Toast.makeText(getContext(), "Database belum ada", Toast.LENGTH_SHORT);
        }
        else
            Toast.makeText(getContext(), "Database sudah ada", Toast.LENGTH_SHORT);

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

                        poster[i] = "https://image.tmdb.org/t/p/w185" + result.getPosterPath();
                        title[i] = result.getTitle();
                        tahun[i] = result.getReleaseDate();
                        deskripsi[i] = result.getOverview();
                        rating[i] = String.valueOf(result.getVoteCount());
                        genre[i] = "daw";
                        path = "sdf";

                        Log.d("TAG-TITLE", title[i]);
                        Log.d("TAG-POSTER", poster[i]);

                        movies.add(poster[i]);
                    }

                    for(int i=0; i<query.getResults().size(); i++){
                        final int finalI = i;
                        Picasso.with(getActivity()).load(poster[i]).into(new Target(){

                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                path = saveToInternalStorage(bitmap, title[finalI]);
                                Log.d("TAG", "onBitmapLoaded");
                                Log.d("TAG-PATH", path);
                                if(path != null)
                                    insertMovies(title[finalI], tahun[finalI], deskripsi[finalI], rating[finalI], genre[finalI], path);
                                else
                                    Toast.makeText(getActivity(), "path null", Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                Log.d("TAG-ERROR", "error");
                            }

                            @Override
                            public void onPrepareLoad(final Drawable placeHolderDrawable) {
                                Log.d("TAG", "Prepare Load");
                            }
                        });
                    }

                    gridView.setAdapter(new ImageAdapter(getActivity(), movies));
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

    private String saveToInternalStorage(Bitmap bitmapImage, String title){
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, title+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }

    public class GetImages extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (result != null) {

                // New data is back from the server.  Hooray!
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
