package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = "Profile";
    private final int REQUEST_CODE = 20;
    private SwipeRefreshLayout scTweets;

    User user;
    List<Tweet> tweets;

    TwitterClient client;
    RecyclerView rvTweets;
    TweetsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        user = (User) Parcels.unwrap(getIntent().getParcelableExtra("user"));
        Log.d(TAG, "Showing profile for " + user.name);

        client = TwitterApp.getRestClient(this);
        rvTweets = findViewById(R.id.rvProfileTweets);

        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // recycler view setup: layout manager and adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);
        populateProfileTimeline();

        // lookup the swipe container view
        scTweets = (SwipeRefreshLayout) findViewById(R.id.scProfileTweets);
        // setup refresh listener which triggers new data loading
        scTweets.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // code to refresh list
                // call scTweets.setRefreshing(false) once network request has completed successfully
                fetchTimelineAsync(0);
            }
        });
    }

    public void fetchTimelineAsync(int page) {
        // send network request to fetch updated date
        // client here is an instance of AAndroid Async HTTP
        // getHomeTimeline is an example endpoint
        client.getProfileTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess retrieved new profile timeline");
                // clear out all home timeline
                adapter.clear();

                try {
                    // new data has come back, add items to adapter
                    adapter.addAll(Tweet.fromJsonArray(json.jsonArray));
                    // signal refresh has finished
                    scTweets.setRefreshing(false);
                    Log.d(TAG, "replaced old profile tl with refreshed one");
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

    // getting the home time line
    private void populateProfileTimeline() {
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