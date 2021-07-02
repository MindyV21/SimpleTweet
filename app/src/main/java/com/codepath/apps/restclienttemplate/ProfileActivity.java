package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    ImageView ivBanner;
    ImageView ivProfile;
    TextView tvScreenName;
    TextView tvName;
    TextView tvBody;

    FloatingActionButton fabCompose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        user = (User) Parcels.unwrap(getIntent().getParcelableExtra("user"));
        Log.d(TAG, "Showing profile for " + user.name);

        // init/find things
        fabCompose = findViewById(R.id.fabCompose);
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

        // set up fabCompose
//        fabCompose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // compose icon has been selected
//                Log.d(TAG, "compose a tweet clicked");
//                // navigate to compose activity
//                Intent intent = new Intent(ProfileActivity.this, ComposeActivity.class);
//                startActivityForResult(intent, REQUEST_CODE);
//            }
//        });

        // profile
        ivBanner = findViewById(R.id.ivBanner);
        Glide.with(this).load(user.publicBannerUrl).into(ivBanner);

        ivProfile = findViewById(R.id.ivProfileImageActual);
        Glide.with(this).load(user.publicImageUrl).transform(new CircleCrop()).into(ivProfile);

        tvScreenName = findViewById(R.id.tvProfileScreenName);
        tvName = findViewById(R.id.tvProfileName);
        tvBody = findViewById(R.id.tvProfileBody);

        tvScreenName.setText(user.screenName);
        tvName.setText(user.name);
        tvBody.setText(user.description);
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

    // getting the profile time line
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
}