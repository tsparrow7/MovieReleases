package com.example.tjgaming.moviereleases;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by TJ Gaming on 6/17/2016.
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static class DetailFragment extends Fragment {

        //Data for building url
        private String myKey;
        final String API_KEY_PARAM = "api_key";
        final String BASE_JSON_REQUEST = "api.themoviedb.org";
        final String JSON_REQUEST_PARAM = "3";
        final String MOVIE_JSON_REQUEST = "movie";
        final String REVIEW_JSON_REQUEST = "reviews";
        final String TRAILER_JSON_REQUEST = "videos";

        //For youtube url
        final String YOUTUBE_BASE_URL = "www.youtube.com";
        final String YOUTUBE_WATCH_PARAM = "watch";
        final String YOUTUBE_VIDEO_ID_QUERY_PARAM = "v";

        //For poster image
        final String BASE_POSTER_URL = "http://image.tmdb.org/t/p/";
        final String POSTER_SIZE_PARAM = "w370";

        //Data to retrieve from the movie API url
        private String movieTitle = null;
        private String moviePoster = null;
        private String movieOverview = null;
        private String movieRating = null;
        private String movieReleaseDate = null;
        private String movieId = null;
        private String movieTrailer = null;
        private String movieTrailerUrl = null;
        private String movieReview = null;
        private String movieReviewUrl = null;
        private String movieAuthor = null;
        private String movieContent = null;

        //TextViews to populate with data
        private TextView movieTitleTextView;
        private TextView movieReleaseDateTextView;
        private TextView movieRatingTextView;
        private TextView movieOverviewTextView;
        private TextView movieReviewTextView;
        private ImageView poster;
        private TextView movieTrailerTextView;

        public void DetailsFragment() {}

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {

                return false;
            }

            return super.onOptionsItemSelected(item);
        }


        public void getMovieReview(String url) {

            //Network Check
            if (NetworkChecker.isNetworkActive(getActivity())) {
                try {

                    GetMovieTask movieReviewTask = new GetMovieTask();
                    String review = movieReviewTask.execute(url).get();

                    JSONObject reviewObject = new JSONObject(review);
                    JSONArray reviewArray = reviewObject.getJSONArray("results");

                    for (int i = 0; i < reviewArray.length(); i++) {

                        JSONObject jsonObject = reviewArray.getJSONObject(i);

                        movieAuthor = jsonObject.getString("author");
                        movieContent = jsonObject.getString("content");

                    }

                }catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        public void getMovieTrailer(String url) {

            //Network check
            if (NetworkChecker.isNetworkActive(getActivity())) {

                try {

                    GetMovieTask movieTrailerTask = new GetMovieTask();
                    String trailer = movieTrailerTask.execute(url).get();

                    JSONObject trailerObject = new JSONObject(trailer);
                    JSONArray trailerArray = trailerObject.getJSONArray("results");

                    for (int i = 0; i < trailerArray.length(); i++) {

                        JSONObject jsonObject = trailerArray.getJSONObject(i);

                        movieTrailer = jsonObject.getString("key");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setDetailView() {

            //Network Check
            if (NetworkChecker.isNetworkActive(getActivity())) {

                String baseUrl = buildUrl("Base");

                movieTrailerUrl = buildUrl("Trailer");

                movieReviewUrl = buildUrl("Review");

                try {

                    GetMovieTask movieTask = new GetMovieTask();
                    String movieData = movieTask.execute(baseUrl).get();

                    JSONObject jsonObject = new JSONObject(movieData);

                    moviePoster = jsonObject.getString("poster_path");
                    movieOverview = jsonObject.getString("overview");
                    movieTitle = jsonObject.getString("original_title");
                    movieReleaseDate = jsonObject.getString("release_date");
                    movieRating = jsonObject.getString("vote_average");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                getMovieReview(movieReviewUrl);

                movieReview = movieContent + "\nWritten by: " + movieAuthor;

                //set the poster for the movie
                loadPosterImage();

                //Set data for the textViews
                setTextViews();

                movieTrailerTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Get the youtube extension for the current movie trailer
                        getMovieTrailer(movieTrailerUrl);

                        //Build the youtube url to watch trailer
                        Uri.Builder youtubeBuilder = new Uri.Builder();
                        youtubeBuilder.scheme("https");
                        youtubeBuilder.authority(YOUTUBE_BASE_URL);
                        youtubeBuilder.appendPath(YOUTUBE_WATCH_PARAM);
                        youtubeBuilder.appendQueryParameter(YOUTUBE_VIDEO_ID_QUERY_PARAM, movieTrailer);

                        String youtubeUrl = youtubeBuilder.build().toString();

                        //Launch youtube intent to watch movie trailer
                        launchYoutubeIntent(youtubeUrl);
                    }
                });
            }
        }

        //Builds Url based on type of data needed to receive
        public String buildUrl(String typeOfUrl) {

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https");
            builder.authority(BASE_JSON_REQUEST);
            builder.appendPath(JSON_REQUEST_PARAM);
            builder.appendPath(MOVIE_JSON_REQUEST);
            builder.appendPath(movieId);

            if (typeOfUrl.equalsIgnoreCase("Base")) {

                builder.appendQueryParameter(API_KEY_PARAM, myKey);
                return builder.build().toString();
            } else if (typeOfUrl.equalsIgnoreCase("Trailer")) {

                builder.appendPath(TRAILER_JSON_REQUEST);
                builder.appendQueryParameter(API_KEY_PARAM, myKey);
                return builder.build().toString();

            } else if (typeOfUrl.equalsIgnoreCase("Review")) {

                builder.appendPath(REVIEW_JSON_REQUEST);
                builder.appendQueryParameter(API_KEY_PARAM, myKey);
                return builder.build().toString();
            }

            return null;
        }

        //assigns data to the textViews
        public void setTextViews() {

            movieTitleTextView.setText(movieTitle);

            movieReleaseDateTextView.setText(
                    "Release Date: " + movieReleaseDate);

            movieRatingTextView.setText(
                    "Rating: " + movieRating + "/10");

            movieOverviewTextView.setText(
                    "Overview: " + movieOverview);

            movieReviewTextView.setText(movieReview);

            movieTrailerTextView.setText("Watch Trailer");
        }

        public void launchYoutubeIntent(String url) {

            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW);
//            youtubeIntent.setPackage("com.google.android.youtube");
            youtubeIntent.setData(Uri.parse(url));

            startActivity(youtubeIntent);
        }

        public void loadPosterImage() {

            Picasso.with(getContext())
                    .load(BASE_POSTER_URL+POSTER_SIZE_PARAM+moviePoster)
                    .into(poster);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

            View view = inflater.inflate(R.layout.fragment_detail, container, false);

            movieTitleTextView = (TextView) view.findViewById(R.id.movie_title);
            movieReleaseDateTextView = (TextView)view.findViewById(R.id.movie_release_date);
            movieOverviewTextView = (TextView)view.findViewById(R.id.movie_overview);
            movieRatingTextView = (TextView)view.findViewById(R.id.movie_rating);
            movieTrailerTextView = (TextView)view.findViewById(R.id.movie_trailer_link);
            poster = (ImageView)view.findViewById(R.id.poster_image);
            movieReviewTextView = (TextView)view.findViewById(R.id.movie_review);

            myKey = getString(R.string.api_key);

            Intent intent = getActivity().getIntent();
            Bundle arguments = getArguments();

            if (arguments != null) {
                movieId = arguments.getString("MOVIEID");

            }
            else if (arguments == null) {

                try {

                    movieId = intent.getStringExtra("MOVIEID");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            setDetailView();

            setHasOptionsMenu(true);

            return view;
        }
    }
}
