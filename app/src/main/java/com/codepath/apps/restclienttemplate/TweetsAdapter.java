package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static final String TAG = "TweetsAdapter";

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

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvRelativeTimestamp;
        ImageView ivMedia;
        ImageButton ibRetweet;
        ImageButton ibLike;

        // itemView is a representation of one row in recycler view -> a tweet
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvRelativeTimestamp = itemView.findViewById(R.id.tvRelativeTimestamp);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ibRetweet = itemView.findViewById(R.id.ibRetweet);
            ibLike = itemView.findViewById(R.id.ibLike);
        }

        // take tweet attributes to fill out views
        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvRelativeTimestamp.setText(Tweet.getRelativeTimestamp(tweet.createdAt));

            // load in profile image with glide
            Glide.with(context).load(tweet.user.publicImageUrl).into(ivProfileImage);

            if (tweet.imageUrl == null) {
                ivMedia.setVisibility(View.GONE);
            } else {
                Glide.with(context).load(tweet.imageUrl).into(ivMedia);
            }

            // retweet
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_retweet_tweet);
            ibRetweet.setBackground(drawable);
            ibRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ibRetweet.isSelected()) {
                        Log.d(TAG, "un retweet");
                        ibRetweet.setSelected(false);
                    } else {
                        Log.d(TAG, "retweet");
                        ibRetweet.setSelected(true);
                    }

                }
            });

            // like
            drawable = AppCompatResources.getDrawable(context, R.drawable.ic_heart_tweet);
            ibLike.setBackground(drawable);
            ibLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ibLike.isSelected()) {
                        Log.d(TAG, "unlike");
                        ibLike.setSelected(false);
                    } else {
                        Log.d(TAG, "like");
                        ibLike.setSelected(true);
                    }

                }
            });
        }
    }
}
