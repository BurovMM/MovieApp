package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.adapters.ReviewAdapter;
import com.example.movieapp.adapters.TrailerAdapter;
import com.example.movieapp.data.FavoriteMovie;
import com.example.movieapp.data.MainViewModel;
import com.example.movieapp.data.Movie;
import com.example.movieapp.data.Review;
import com.example.movieapp.data.Trailer;
import com.example.movieapp.utils.JSONUtils;
import com.example.movieapp.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private ImageView imageViewBigPoster;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewOverview;
    private ImageView imageViewAddToFavorite;
    private ScrollView scrollViewInfo;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;

    private int id;

    private MainViewModel viewModel;
    private Movie movie;
    private FavoriteMovie favoriteMovie;

    private static String lang;




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itemMain:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.itemFavorite:
                Intent intentToFavorite = new Intent(this, FavoriteActivity.class);
                startActivity(intentToFavorite);
                break;


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imageViewBigPoster = findViewById(R.id.imageViewBigPoster);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle= findViewById(R.id.textViewOriginalTitle);
        textViewRating= findViewById(R.id.textViewRating);
        textViewReleaseDate= findViewById(R.id.textViewReleaseDate);
        textViewOverview= findViewById(R.id.textViewOverview);
        imageViewAddToFavorite = findViewById(R.id.imageViewAddToFavorite);
        scrollViewInfo = findViewById(R.id.scrollViewInfo);

        lang = Locale.getDefault().getLanguage();


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else {
            finish();
        }

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        movie = viewModel.getMovieById(id);

        Picasso.get().load(movie.getBigPosterPath())
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageViewBigPoster);

        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewOverview.setText(movie.getOverview());
        textViewRating.setText(String.format("%f", movie.getVoteAverage()));
        textViewReleaseDate.setText(movie.getReleaseDate());
        setFavorite();



        recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);

        reviewAdapter = new ReviewAdapter();
        trailerAdapter = new TrailerAdapter();

        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
                Toast.makeText(DetailActivity.this, url, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewReviews.setAdapter(reviewAdapter);
        recyclerViewTrailers.setAdapter(trailerAdapter);

        JSONObject jsonObjectTrailers = NetworkUtils.getJSONForVideos(movie.getId(), lang);
        JSONObject jsonObjectReviews = NetworkUtils.getJSONForReviews(movie.getId(), lang);

        ArrayList<Trailer> trailers = JSONUtils.getTrailersFromJSON(jsonObjectTrailers);
        ArrayList<Review> reviews = JSONUtils.getReviewsFromJSON(jsonObjectReviews);

        reviewAdapter.setReviews(reviews);
        trailerAdapter.setTrailers(trailers);

        scrollViewInfo.smoothScrollTo(0,0);
    }

    public void onClickChangeFavorite(View view) {
        if (favoriteMovie == null) {
            viewModel.insertFavoriteMovie(new FavoriteMovie(movie));
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.deleteFavoriteMovie(favoriteMovie);
            Toast.makeText(this, "Deleted from favorites", Toast.LENGTH_SHORT).show();
        }
        setFavorite();
    }

    private void setFavorite() {
        favoriteMovie = viewModel.getFavoriteMovieById(id);
        if (favoriteMovie == null) {
            imageViewAddToFavorite.setImageResource(R.drawable.btn_star_big_off);
        } else {
            imageViewAddToFavorite.setImageResource(R.drawable.btn_star_big_on_pressed);
        }
    }
}
