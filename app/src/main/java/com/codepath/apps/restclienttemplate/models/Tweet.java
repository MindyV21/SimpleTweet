package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {

    public static final String TAG = "TweetModel";

    public String id;
    public String body;
    public String createdAt;
    public User user;
    public String imageUrl;

    // TODO: retweet count, already retweeted, favorited count, already favorited
    public int retweetCount;
    public boolean isRetweeted;
    public int favoriteCount;
    public boolean isFavorited;

    // time constants
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    // empty constructor needed by Parceler Library
    public Tweet() {}

    // turn json into Tweet object
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.id = jsonObject.getString("id_str");
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));

        // check if tweet has any media - doing single image currently
        JSONObject entities = jsonObject.getJSONObject("entities");
        if (entities.has("media")) {
            JSONArray media = entities.getJSONArray("media");
            // FOR int i = 0; i > medialgn
            tweet.imageUrl = media.getJSONObject(0).getString("media_url_https");
        }
        else{
            tweet.imageUrl = null;
        }

        // retweets and favorites data
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.isRetweeted = jsonObject.getBoolean("retweeted");
        tweet.favoriteCount = jsonObject.getInt("favorite_count");
        tweet.isFavorited = jsonObject.getBoolean("favorited");

        return tweet;
    }

    // return a list of tweet objects from a json array
    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    // gets relative timestamp based on createdAt tweet property
    public static String getRelativeTimestamp(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        // calculates time ago with abbreviations
        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (ParseException e) {
            Log.i(TAG, "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }
}
