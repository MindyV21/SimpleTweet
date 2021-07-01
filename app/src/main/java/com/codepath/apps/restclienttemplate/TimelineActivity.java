package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

// creating a timeline
public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;
    private SwipeRefreshLayout scTweets;

    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;

    FloatingActionButton fabCompose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        // init/find things
        fabCompose = findViewById(R.id.fabCompose);

        // find recycler view
        rvTweets = findViewById(R.id.rvTweets);
        // init list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // recycler view setup: layout manager and adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);
        populateHomeTimeline();

        // lookup the swipe container view
        scTweets = (SwipeRefreshLayout) findViewById(R.id.scTweets);
        // setup refresh listener which triggers new data loading
        scTweets.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // code to refresh list
                // call scTweets.setRefreshing(false) once network request has completed successfully
                fetchTimelineAsync(0);
            }
        });
        // configure refreshing colors
//        scTweets.setColorSchemeColors(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);

        // set up fabCompose
//        Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.vector_compose_dm_shortcut);
//        fabCompose.setBackground(drawable);
        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // compose icon has been selected
                Log.d(TAG, "compose a tweet clicked");
                // navigate to compose activity
                Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    public void fetchTimelineAsync(int page) {
        // send network request to fetch updated date
        // client here is an instance of AAndroid Async HTTP
        // getHomeTimeline is an example endpoint
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess retrieved new home timeline");
                // clear out all home timeline
                adapter.clear();

                try {
                    // new data has come back, add items to adapter
                    adapter.addAll(Tweet.fromJsonArray(json.jsonArray));
                    // signal refresh has finished
                    scTweets.setRefreshing(false);
                    Log.d(TAG, "replaced old home tl with refreshed one");
                } catch (JSONException e) {
                    Log.e(TAG, "failed to add tweets to tl after composing " + e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retrieve home timeline " + throwable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // return true bc menu is displayed
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            Log.i(TAG, "User logged out");
            // forget who is logged in
            client.clearAccessToken();
            // pop stack, navigating backwards to login screen
            finish();
            // true to consume menu item selected
            return true;
        }

        // true to consume menu item selected
        return super.onOptionsItemSelected(item);
    }

    // when a tweet is sent back as a result is created, INSERT a new tweet into arraylist
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check request code is same as one passed in
        // and result code (android thing) is valid
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // use data parameter
            //Tweet tweet = (Tweet) data.getSerializableExtra("tweet");
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            // modify data source of tweets
            tweets.add(0, tweet);
            // update adapter
            adapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
            Log.i(TAG, "posting tweet successful!");
        }

    }

    // getting the home time line
    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess!" + json.toString());

                // get timeline of tweets from json
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure!" + response, throwable);
            }
        });
    }
}