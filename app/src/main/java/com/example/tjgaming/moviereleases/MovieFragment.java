package com.example.tjgaming.moviereleases;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A placeholder fragment containing a simple view.
 * // TODO: 6/15/2016 Create a DetailsActivity class and create a SettingsActivity class.
 */
public class MovieFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    //ArrayLists to store the id of the movie and to store the poster url of the movie
    private ArrayList<String> movieIdList = new ArrayList<String>();
    private ArrayList<String> posterUrlList = new ArrayList<String>();

    //Id of the movie from the API and the path of the poster picture.
    private String movieId = null;
    private String poster = null;


    //JSON data for getting movies from the API
    final String BASE_POSTER_URL = "http://image.tmdb.org/t/p/";
    final String BASE_JSON_REQUEST = "api.themoviedb.org";
    final String JSON_REQUEST_PARAM = "3";
    final String MOVIE_JSON_REQUEST = "movie";
    final String API_KEY_PARAM = "api_key";
    final String POSTER_SIZE_PARAM = "w370";
    final String POPULAR_MOVIES_PARAM = "movie";
    final String TOP_RATED_PARAM = "/top-rated";

    //Decides how the movies are sorted, either by popularity or by rating
    private String sortParameter = null;

    int count = 0;
    private GridView mGridView;
    private String mKey;
    private Intent detailsIntent;

    public MovieFragment() {}


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String mKey) {

        ListPreference listPreference = (ListPreference) findPreference(getString(R.string.pref_sort_key));
        listPreference.setSummary(getString(R.string.pref_sort_key));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public String getPoster(String movieId){

        String posterUrl ="";

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http");
            builder.authority(BASE_JSON_REQUEST);
            builder.appendPath(JSON_REQUEST_PARAM);
            builder.appendPath(MOVIE_JSON_REQUEST);
            builder.appendPath(movieId);
            builder.appendQueryParameter(API_KEY_PARAM, mKey);
            String myUrl = builder.build().toString();
            Log.i("URL", myUrl);

            if(NetworkChecker.isNetworkActive(getActivity())){

                GetMovieTask posterPathTask = new GetMovieTask();
                String jsonData = posterPathTask
                        .execute(myUrl)
                        .get();
                JSONObject jsonObject = new JSONObject(jsonData);
                String posterPath = jsonObject.getString("poster_path");
                posterUrl = BASE_POSTER_URL+POSTER_SIZE_PARAM+posterPath;
            }
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        catch (ExecutionException e){
            e.printStackTrace();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return posterUrl;
    }

    public void getMovieDataFromApi(){

        String htmlData = "";

        try {
            if(NetworkChecker.isNetworkActive(getActivity())) {
                //https://www.themoviedb.org/movie
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https");
                builder.authority("www.themoviedb.org");
                builder.appendPath(sortParameter);
                String myUrl = builder.build().toString();

                //retrieves html data from themoviedb.org and sets it to the htmlData variable
                GetMovieTask movieTask = new GetMovieTask();
                htmlData = movieTask.execute(myUrl).get();

            }
            if (htmlData != null) {

                //splits the webpage source code to ignore unnecessary code
                String[] splitHtmlData = htmlData.split("<div class=\"pagination\">");

                //picks out movie id's from web page source code
                Pattern idPattern = Pattern.compile("id=\"movie_(.*?)\"");
                Matcher idMatcher = idPattern.matcher(splitHtmlData[0]);

                while (idMatcher.find()) {

                    movieIdList.add(idMatcher.group(1));
                }

                for (int i = 0; i < movieIdList.size(); i++) {
                    count++;
                    movieIdList.remove(count);
                }

                //creates new Movie objects that store movie id and poster url
                for (int i = 0; i < movieIdList.size(); i++) {
                    //create a movie object, originally both args are null
                    Movie movie = new Movie(movieId, poster);
                    movieId = movieIdList.get(i);
                    movie.setMovieId(movieId);
                    poster = getPoster(movie.getMovieId());
                    movie.setMoviePosterUrl(poster);
                    posterUrlList.add(movie.getMoviePosterUrl());
                }

                String[] postersArray = new String[posterUrlList.size()];
                postersArray = posterUrlList.toArray(postersArray);

                String movie = movieIdList.get(0);

                Bundle args = new Bundle();
                args.putString("movieId", movie);
                MovieFragment movieFragment = new MovieFragment();
                movieFragment.setArguments(args);

                GridViewAdapter adapter = new GridViewAdapter(getActivity(), getId(), postersArray);
                mGridView.setAdapter(adapter);
                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String movie = movieIdList.get(position);

                        //Start a new intent launching the detailActivity class

                        detailsIntent = new Intent(getActivity(), DetailActivity.class);
                        detailsIntent.putExtra("MOVIEID", movie);
                        startActivity(detailsIntent);

                    }
                });
            }
            else{
                Toast.makeText(getActivity(), "Network currently not available", Toast.LENGTH_LONG)
                        .show();
            }
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        catch (ExecutionException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.movies_grid_view, container, false);
        mGridView = (GridView) v.findViewById(R.id.grid_view);
        mKey = getString(R.string.api_key);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortPref = sharedPreferences.getString(getString(R.string.pref_sort_key),getString(R.string.pref_sort_popular));

        if(sortPref.equals(getString(R.string.pref_sort_rating))){

            sortParameter = POPULAR_MOVIES_PARAM+TOP_RATED_PARAM;
            getMovieDataFromApi();
        }
        else if(sortPref.equals("popular")){

            sortParameter = POPULAR_MOVIES_PARAM;
            getMovieDataFromApi();
        }
        return v;
    }

    //Custom Array Adapter
    public class GridViewAdapter extends ArrayAdapter{

        private Context context;
        private LayoutInflater inflater;
        private int id;
        private String[] imageURls;

        GridViewAdapter(Context context, int id, String[] imageUrls){

            super(context, R.layout.movies_grid_view, imageUrls);

            this.context = context;
            this.id = id;
            this.imageURls = imageUrls;

            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            if(convertView == null){

                convertView = inflater.inflate(R.layout.grid_view_image_view, parent, false);
            }
            Picasso.with(context).load(imageURls[position]).fit().into((ImageView) convertView);
            return convertView;
        }
    }
}