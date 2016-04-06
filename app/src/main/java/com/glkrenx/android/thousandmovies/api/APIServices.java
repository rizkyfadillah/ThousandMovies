package com.glkrenx.android.thousandmovies.api;

import com.glkrenx.android.thousandmovies.model.Movies;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by GLkrenx on 05/04/2016.
 */
public interface APIServices {
    public static final String API_URL = "http://api.themoviedb.org/3/discover/";

    @GET("movie?")
    Call<Movies> getMovies(@Query("page") int page,
                            @Query("api_key") String apiKey);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    APIServices apiServices = retrofit.create(APIServices.class);
}
