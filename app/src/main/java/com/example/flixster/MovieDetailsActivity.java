package com.example.flixster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.flixster.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

public class MovieDetailsActivity extends AppCompatActivity {

    Movie movie;

    public static final String TAG = "MovieDetailsActivity";

    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView ivPoster;
    TextView tvRuntime;
    TextView tvBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        //set view objects
        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        rbVoteAverage = findViewById(R.id.rbVoteAverage);
        ivPoster = findViewById(R.id.ivPoster);
        tvRuntime = findViewById(R.id.tvRuntime);
        tvBudget = findViewById(R.id.tvBudget);

        // unwrap the movie that has been passed through the intent
        movie = Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // set the title, overview, runtime, budget fields
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        if(movie.getRuntime() != 0) {
            tvRuntime.setText(movie.getRuntime());
        } else
        {
            tvRuntime.append("unknown");
        }

        //tvRuntime.setText(movie.getRuntime());

        if(movie.getBudget() != 0) {
            tvBudget.setText(movie.getBudget());
        } else
        {
            tvBudget.append("unknown");
        }

        // API call for YouTube propagation
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://api.themoviedb.org/3/movie/"+movie.getId()+"/videos?api_key=bed14e337854ed86aad3f65613a20233", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray results = jsonObject.getJSONArray("results");
                    Log.i(TAG, "Results: " + results.toString());
                    JSONObject jObj = results.getJSONObject(0);
                    String movieId = jObj.getString("key");
                    movie.setMovieId(movieId);
                    Log.i(TAG, "MovieId: " + movieId);
                } catch (JSONException e) {
                    Log.e(TAG, "Hit json exception", e);
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure");
            }
        });

        // listener
        ivPoster.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //create an intent to launch the movie trailer activity
                Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                //pass the video id as a string extra
                intent.putExtra("movieId", movie.getMovieId());
                //show the activity
                MovieDetailsActivity.this.startActivity(intent);
            }
        });

        //set rating
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage / 2.0f);

        //set image
        Glide.with(this)
                .load(movie.getBackdropPath())
                .placeholder(R.drawable.flicks_backdrop_placeholder)
                .transform(new RoundedCorners(30))
                .into(ivPoster);
    }
}