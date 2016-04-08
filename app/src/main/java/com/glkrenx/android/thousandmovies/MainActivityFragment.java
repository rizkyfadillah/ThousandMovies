package com.glkrenx.android.thousandmovies;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.glkrenx.android.thousandmovies.api.APIServices;
import com.glkrenx.android.thousandmovies.db.MoviesDb;
import com.glkrenx.android.thousandmovies.db.MoviesDbHelper;
import com.glkrenx.android.thousandmovies.model.Movies;
import com.glkrenx.android.thousandmovies.model.Result;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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

    private List<String> movies;
    private GridView gridView;
    private String [] title, poster, tahun, deskripsi, rating, genre, paaath;
    private MoviesDbHelper dbHelper;
    private SQLiteDatabase db;
    private Call<Movies> moviesCall;
    private OnItemSelectedListener listener;
    private Cursor cursor;
    private int flag;
    private ProgressDialog pDialog;

    public static final int FLAG_DATABASE = 0;
    public static final int FLAG_INTERNET = 1;

    public MainActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] listMovies = {};
        movies = new ArrayList<>(Arrays.asList(listMovies));

        dbHelper = new MoviesDbHelper(getActivity());
        db = dbHelper.getWritableDatabase();

        gridView = (GridView) rootView.findViewById(R.id.gridview);

        poster = new String[30];
        title = new String[30];
        genre = new String[30];
        tahun = new String[30];
        deskripsi = new String[30];
        rating = new String[30];
        paaath = new String[30];

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Result result = new Result();

                result.setTitle(title[position]);
                result.setOverview(deskripsi[position]);
                result.setReleaseDate(tahun[position]);
                result.setPosterPath(paaath[position]);
                result.setVoteCount(Integer.valueOf(rating[position]));

                listener.onMoviesItemSelected(result, flag);
            }
        });

        cursor = db.query(
                MoviesDb.Movies.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        /*Result result = new Result();

        result.setTitle(title[0]);
        result.setOverview(deskripsi[0]);
        result.setReleaseDate(tahun[0]);
        result.setPosterPath(paaath[0]);
        result.setVoteCount(Integer.valueOf(rating[0]));

        listener.onMoviesItemSelected(result, flag);*/

        return rootView;
    }

    private void gunakanDatabase() {
        Toast.makeText(getContext(), "Menggunakan data dari database", Toast.LENGTH_SHORT).show();
        int counter = 0;
        while(cursor.moveToNext()){
            movies.add(cursor.getString(cursor.getColumnIndexOrThrow("path")));
            title[counter] = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            deskripsi[counter] = cursor.getString(cursor.getColumnIndexOrThrow("deskripsi"));
            rating[counter] = cursor.getString(cursor.getColumnIndexOrThrow("rating"));
            tahun[counter] = cursor.getString(cursor.getColumnIndexOrThrow("tahun"));
            paaath[counter] = cursor.getString(cursor.getColumnIndexOrThrow("path"));
            counter++;
        }
        flag = FLAG_DATABASE;
        gridView.setAdapter(new ImageAdapter(getActivity(), movies, ImageAdapter.FLAG_PATH));
    }


    @Override
    public void onStart() {
        super.onStart();

        int counter = 0;
        while(cursor.moveToNext()){
            counter++;
        }

        if(isNetworkConnected()){
            db.delete(MoviesDb.Movies.TABLE_NAME, null, null);
            loadAPI();
        } else if(!isNetworkConnected() & !cursor.moveToFirst() || counter<20) {
            Toast.makeText(getContext(), "Database belum ada dan tidak ada koneksi. Aktifkan terlebih dahulu internet anda.", Toast.LENGTH_SHORT).show();
        } else if(!isNetworkConnected() & cursor.moveToFirst() || counter>=20) {
            Toast.makeText(getContext(), "Tidak ada koneksi internet. Maka akan menggunakan database.", Toast.LENGTH_SHORT).show();
            gunakanDatabase();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void loadAPITanpaUpdateDatabase() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sorting = sharedPreferences.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popularity_value_default));
        String year = sharedPreferences.getString(getString(R.string.pref_year_key),
                getString(R.string.pref_year_value_default));
        String genres = sharedPreferences.getString(getString(R.string.pref_genre_key),
                getString(R.string.pref_genre_value_default));

        findData(genres, year, sorting);

        movies.clear();

        moviesCall.enqueue(new Callback<Movies>() {
            @Override
            public void onResponse(Call<Movies> call, Response<Movies> response) {
                if (response.isSuccessful()) {
                    Movies query = response.body();
                    for (int i = 0; i < query.getResults().size(); i++) {
                        Result result = query.getResults().get(i);

                        if (result.getPosterPath() != null) {
                            poster[i] = "https://image.tmdb.org/t/p/w185" + result.getPosterPath();
                        } else {
                            poster[i] = "https://assets.tmdb.org/assets/f996aa2014d2ffddfda8463c479898a3/images/no-poster-w185.jpg";
                        }
                        title[i] = result.getTitle();
                        tahun[i] = result.getReleaseDate();
                        deskripsi[i] = result.getOverview();
                        rating[i] = String.valueOf(result.getVoteCount());
                        genre[i] = "daw";
                        paaath[i] = poster[i];

                        movies.add(poster[i]);
                    }
                    flag = FLAG_INTERNET;
                    gridView.setAdapter(new ImageAdapter(getActivity(), movies, ImageAdapter.FLAG_URL));
                }
            }

            @Override
            public void onFailure(Call<Movies> call, Throwable t) {

            }
        });
    }

    public interface OnItemSelectedListener {
        public void onMoviesItemSelected(Result result, int flag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }


    private void loadAPI(){
        Toast.makeText(getContext(), "Load API", Toast.LENGTH_SHORT).show();

        //db.delete(MoviesDb.Movies.TABLE_NAME, null, null);
        deletePoster();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sorting = sharedPreferences.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popularity_value_default));
        String year = sharedPreferences.getString(getString(R.string.pref_year_key),
                getString(R.string.pref_year_value_default));
        String genres = sharedPreferences.getString(getString(R.string.pref_genre_key),
                getString(R.string.pref_genre_value_default));

        findData(genres, year, sorting);

        movies.clear();

        moviesCall.enqueue(new Callback<Movies>() {
            @Override
            public void onResponse(Call<Movies> call, Response<Movies> response) {
                if (response.isSuccessful()) {
                    Movies query = response.body();
                    for (int i = 0; i < query.getResults().size(); i++) {
                        Result result = query.getResults().get(i);

                        if (result.getPosterPath() != null) {
                            poster[i] = "https://image.tmdb.org/t/p/w185" + result.getPosterPath();
                        } else {
                            poster[i] = "https://assets.tmdb.org/assets/f996aa2014d2ffddfda8463c479898a3/images/no-poster-w185.jpg";
                        }
                        title[i] = result.getTitle();
                        tahun[i] = result.getReleaseDate();
                        deskripsi[i] = result.getOverview();
                        rating[i] = String.valueOf(result.getVoteCount());
                        genre[i] = "daw";

                        movies.add(poster[i]);
                    }
                    flag = FLAG_INTERNET;

                    gridView.setAdapter(new ImageAdapter(getActivity(), movies, ImageAdapter.FLAG_URL));

                    DownloadImages downloadImages = new DownloadImages(poster, title, tahun, deskripsi, rating, genre);
                    downloadImages.execute();
                }
            }

            @Override
            public void onFailure(Call<Movies> call, Throwable t) {

            }
        });
    }

    private Call findData(String genre, String year, String sorting){
        if (sorting.isEmpty() && !year.isEmpty()){
            moviesCall = APIServices.apiServices.getMoviesYear(1,
                    "e7504adfe89a3fc5549f39c63c618e7c", year, genre);
        }else if (year.isEmpty() && !sorting.isEmpty() ){
            moviesCall = APIServices.apiServices.getMoviesSortBy(1,
                    "e7504adfe89a3fc5549f39c63c618e7c", sorting, genre);
        }else if (!year.isEmpty() && !sorting.isEmpty() && genre.isEmpty()){
            moviesCall = APIServices.apiServices.getMoviesSortYear(1,
                    "e7504adfe89a3fc5549f39c63c618e7c", sorting, year);
        }else if (!year.isEmpty() && !sorting.isEmpty() && !genre.isEmpty()){
            moviesCall = APIServices.apiServices.getMoviesFull(1,
                    "e7504adfe89a3fc5549f39c63c618e7c", sorting, year, genre);
        }else if (sorting.isEmpty() && year.isEmpty() && !genre.isEmpty()){
            moviesCall = APIServices.apiServices.getMoviesGenre(1,
                    "e7504adfe89a3fc5549f39c63c618e7c", genre);
        }else {
            moviesCall = APIServices.apiServices.getMoviesDefault(1,
                    "e7504adfe89a3fc5549f39c63c618e7c");
        }
        return moviesCall;
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

        if(locationRowId != -1){}
            //Toast.makeText(getActivity(), "Database masuk", Toast.LENGTH_SHORT).show();
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String title){
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, title+".jpg");

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

    public void deletePoster(){
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        boolean delete = directory.delete();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        dbHelper.close();
    }

    private class DownloadImages extends AsyncTask<String,Void,Bitmap[]> {

        String [] linkPoster;
        String [] title, tahun, deskripsi, rating, genre;

        private final String LOG_TAG = DownloadImages.class.getSimpleName();

        public DownloadImages(String [] linkPoster, String [] title, String [] tahun, String [] deskripsi, String [] rating, String [] genre){
            this.linkPoster = linkPoster;
            this.title=title;
            this.tahun=tahun;
            this.deskripsi=deskripsi;
            this.rating=rating;
            this.genre=genre;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

            /*pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Downloading images... Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);*/
            //pDialog.show();
        }

        public Bitmap downloadImage(String urlString){
            int count = 0;
            Bitmap bitmap = null;

            URL url;
            InputStream inputStream = null;
            BufferedOutputStream outputStream = null;

            try {
                url = new URL(urlString);
                URLConnection connection = url.openConnection();
                int lenghtOfFile = connection.getContentLength();

                inputStream = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();

                outputStream = new BufferedOutputStream(dataStream);

                byte data[] = new byte[512];
                long total = 0;

                while ((count = inputStream.read(data)) != -1) {
                    total += count;
		            /*publishing progress update on UI thread.
		            Invokes onProgressUpdate()*/
                    //publishProgress((int)((total*100)/lenghtOfFile));

                    // writing data to byte array stream
                    outputStream.write(data, 0, count);
                }
                outputStream.flush();

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inSampleSize = 1;

                byte[] bytes = dataStream.toByteArray();
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,bmOptions);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        private Bitmap [] DownloadImageBitmap() {

            Bitmap [] bitmaps = new Bitmap[30];
            HttpURLConnection urlConnection = null;

            for(int i=0; i<linkPoster.length; i++){
                //Log.d(LOG_TAG, poster[i]);
                try {
                    URL url = new URL(poster[i]);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    bitmaps[i] = BitmapFactory.decodeStream(url.openStream());

                    paaath[i] = saveToInternalStorage(bitmaps[i], title[i]);
                    insertMovies(title[i], tahun[i], deskripsi[i], rating[i], genre[i], paaath[i]);
                } catch (IOException e) {
                    Log.e("error", "Downloading Image Failed");
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return bitmaps;
        }

        @Override
        protected Bitmap [] doInBackground(String... params) {
            return DownloadImageBitmap();
            /*Bitmap [] bitmap = new Bitmap[30];
            for(int i=0; i<linkPoster.length; i++){
                bitmap[i] = downloadImage(linkPoster[i]);
            }
            return bitmap;*/
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            super.onPostExecute(bitmaps);
            /*if(bitmaps != null){

            } else {
                Toast.makeText(getContext(), "Downloading Image Failed", Toast.LENGTH_SHORT).show();
            }*/
            //pDialog.dismiss();
        }
    }
}