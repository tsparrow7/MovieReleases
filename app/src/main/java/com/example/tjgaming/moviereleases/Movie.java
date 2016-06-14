package com.example.tjgaming.moviereleases;

/**
 * Created by TJ Gaming on 6/14/2016.
 */
public class Movie {

    private String movieId;
    private String moviePosterUrl;

    public Movie(String movieId, String moviePosterUrl) {
        this.movieId = movieId;
        this.moviePosterUrl = moviePosterUrl;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMoviePosterUrl() {
        return moviePosterUrl;
    }

    public void setMoviePosterUrl(String moviePosterUrl) {
        this.moviePosterUrl = moviePosterUrl;
    }

}
