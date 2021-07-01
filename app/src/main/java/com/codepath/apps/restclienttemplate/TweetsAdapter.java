package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static final String TAG = "TweetsAdapter";

    TwitterClient client;
    Context context;
    List<Tweet> tweets;
    // pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // for each row, inflate the layout for a tweet
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        // inflating a tweet
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // bind values based on the position of each element
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // get data
        Tweet tweet = tweets.get(position);
        // bind the tweet with view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // clean all elements of recycler view
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // add a list of items
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    // define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder {

        // main tweet content
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvName;
        TextView tvRelativeTimestamp;

        // above retweet details - situational
        RelativeLayout rlAbove;
        ImageView ivRetweeted;
        TextView tvRetweeted;

        // media content of tweet
        ImageView ivMedia;

        // bottom icons
        ImageButton ibRetweet;
        ImageButton ibLike;
        TextView tvRetweets;
        TextView tvLikes;

        // itemView is a representation of one row in recycler view -> a tweet
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvRelativeTimestamp = itemView.findViewById(R.id.tvRelativeTimestamp);

            rlAbove = itemView.findViewById(R.id.rlRetweeted);
            ivRetweeted = itemView.findViewById(R.id.ivRetweeted);
            tvRetweeted = itemView.findViewById(R.id.tvRetweeted);

            ivMedia = itemView.findViewById(R.id.ivMedia);

            ibRetweet = itemView.findViewById(R.id.ibRetweet);
            ibLike = itemView.findViewById(R.id.ibLike);
            tvRetweets = itemView.findViewById(R.id.tvRetweets);
            tvLikes = itemView.findViewById(R.id.tvLikes);
        }

        // take tweet attributes to fill out views
        public void bind(final Tweet tweet) {
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_vector_retweet_stroke);

            // ABOVE TWEET - IF RETWEETED
            if (tweet.hasRetweetText) {
                ivRetweeted.setImageDrawable(drawable);
                tvRetweeted.setText(tweet.retweetUserName + " Retweeted");
            } else {
                rlAbove.setVisibility(View.GONE);
            }

            // MAIN TWEET CONTENT
            tvBody.setText(tweet.body);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvName.setText(tweet.user.name);
            tvRelativeTimestamp.setText(" - " + Tweet.getRelativeTimestamp(tweet.createdAt));

            // load in profile image with glide
            Glide.with(context).load(tweet.user.publicImageUrl).transform(new CircleCrop()).into(ivProfileImage);

            // MEDIA CONTENT
            if (tweet.imageUrl == null) {
                ivMedia.setVisibility(View.GONE);
            } else {
                Glide.with(context).load(tweet.imageUrl).into(ivMedia);
            }

            // BOTTOM ICONS
            // retweet
            tvRetweets.setText("" + tweet.retweetCount);
            drawable = AppCompatResources.getDrawable(context, R.drawable.ic_retweet_tweet);
            ibRetweet.setBackground(drawable);
            if (tweet.isRetweeted) {
                ibRetweet.setSelected(true);
            } else {
                ibRetweet.setSelected(false);
            }
            ibRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ibRetweet.isSelected()) {
                        Log.d(TAG, "un retweet");
                        unretweetTweet(tweet.id);
                    } else {
                        Log.d(TAG, "retweet");
                        retweetTweet(tweet.id);
                    }

                }
            });

            // like
            tvLikes.setText("" + tweet.favoriteCount);
            drawable = AppCompatResources.getDrawable(context, R.drawable.ic_heart_tweet);
            ibLike.setBackground(drawable);
            if (tweet.isFavorited) {
                ibLike.setSelected(true);
            } else {
                ibLike.setSelected(false);
            }
            ibLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ibLike.isSelected()) {
                        Log.d(TAG, "unlike");
                        unfavoriteTweet(tweet.id);
                    } else {
                        Log.d(TAG, "like");
                        favoriteTweet(tweet.id);
                    }

                }
            });
        }

        public void retweetTweet(String id) {
            client = TwitterApp.getRestClient(context);
            client.retweetTweet(id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.d(TAG, "OnSuccess! Twitter retweet api call: " + json.toString());
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "OnFailure! Twitter retweet api call: " + response, throwable);
                }
            });
            ibRetweet.setSelected(true);
        }

        public void unretweetTweet(String id) {
            client = TwitterApp.getRestClient(context);
            client.unretweetTweet(id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.d(TAG, "OnSuccess! Twitter unretweet api call: " + json.toString());
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "OnFailure! Twitter unretweet api call: " + response, throwable);
                }
            });
            ibRetweet.setSelected(false);
        }

        public void favoriteTweet(String id) {
            client = TwitterApp.getRestClient(context);
            client.favoriteTweet(id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.d(TAG, "OnSuccess! Twitter favorite api call: " + json.toString());
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "OnFailure! Twitter favorite api call: " + response, throwable);
                }
            });
            ibLike.setSelected(true);
        }

        public void unfavoriteTweet(String id) {
            client = TwitterApp.getRestClient(context);
            client.unfavoriteTweet(id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.d(TAG, "OnSuccess! Twitter unfavorite api call: " + json.toString());
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "OnFailure! Twitter unfavorite api call: " + response, throwable);
                }
            });
            ibLike.setSelected(false);
        }
    }
}
