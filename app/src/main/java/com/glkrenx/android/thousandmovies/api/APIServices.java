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
    Call<Movies> getMoviesFull(@Query("page") int page,
                               @Query("api_key") String apiKey,
                               @Query("sort_by") String sort_by,
                               @Query("primary_release_year") String year,
                               @Query("with_genres") String genre);
    @GET("movie?")
    Call<Movies> getMoviesDefault(@Query("page") int page,
                                  @Query("api_key") String apiKey);

    @GET("movie?")
    Call<Movies> getMoviesSortBy(@Query("page") int page,
                                 @Query("api_key") String apiKey,
                                 @Query("sort_by") String sort_by,
                                 @Query("with_genres") String genre);

    @GET("movie?")
    Call<Movies> getMoviesYear(@Query("page") int page,
                               @Query("api_key") String apiKey,
                               @Query("primary_release_year") String year,
                               @Query("with_genres") String genre);

    @GET("movie?")
    Call<Movies> getMoviesGenre(@Query("page") int page,
                                @Query("api_key") String apiKey,
                                @Query("with_genres") String genre);

    @GET("movie?")
    Call<Movies> getMoviesSortYear(@Query("page") int page,
                                   @Query("api_key") String apiKey,
                                   @Query("sort_by") String sort_by,
                                   @Query("primary_release_year") String year);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    APIServices apiServices = retrofit.create(APIServices.class);
}
