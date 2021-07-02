package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {

    public String name;
    public String screenName;
    public String publicImageUrl; // profile pic

    // TODO: add
    public String description; // nullable
    public boolean verified;
    public int followersCount;
    public int followingCount;
    public String publicBannerUrl;

    // empty constructor needed by Parceler Library
    public User() {}

    // create a User from a json object
    public static User fromJson(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        user.publicImageUrl = jsonObject.getString("profile_image_url_https");

        user.description = jsonObject.getString("description");
        user.verified = jsonObject.getBoolean("verified");
        user.followersCount = jsonObject.getInt("followers_count");
        user.followingCount = jsonObject.getInt("friends_count");
        user.publicBannerUrl = jsonObject.getString("profile_banner_url");

        return user;
    }
}
