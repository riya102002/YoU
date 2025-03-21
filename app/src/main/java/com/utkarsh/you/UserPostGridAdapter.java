package com.utkarsh.you;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserPostGridAdapter extends BaseAdapter {

    private List<UserPost> userPostList;

    public UserPostGridAdapter(List<UserPost> userPostList) {
        this.userPostList = userPostList;
    }

    @Override
    public int getCount() {
        return userPostList.size();
    }

    @Override
    public Object getItem(int position) {
        return userPostList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate or reuse grid item layout
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_user_post, parent, false);
        }

        // Bind data to grid item views
        UserPost userPost = userPostList.get(position);

        // Find views in grid item layout
        ImageView postImg = convertView.findViewById(R.id.profilePost);
        TextView captionTextView = convertView.findViewById(R.id.captionTextView);
        TextView descriptionTextView = convertView.findViewById(R.id.descriptionTextView);
        TextView keywordsTextView = convertView.findViewById(R.id.keywordsTextView);
        TextView authorPostTextView = convertView.findViewById(R.id.authorPostTextView);
        CircleImageView profileImageView = convertView.findViewById(R.id.profileAuthor); // Add profile image view

        // Set data to grid item views
        Glide.with(parent.getContext())
                .load(userPost.getImageUrl())
                .into(postImg);
        captionTextView.setText(userPost.getCaption());
        descriptionTextView.setText(userPost.getDescription());
        keywordsTextView.setText(userPost.getKeywords());
        authorPostTextView.setText(userPost.getAuthor());

        // Set profile image
        Glide.with(parent.getContext())
                .load(userPost.getProfileImageUrl())
                .placeholder(R.drawable.youicon)
                .into(profileImageView);

        return convertView;
    }
}


class UserPost {
    private String caption;
    private String description;
    private String keywords;
    private String imageUrl;
    private String authorId;
    private String author;
    private String profileImageUrl; // New field for profile image URL

    // Constructor, getters, and setters
    public UserPost(){}

    public UserPost(String caption, String description, String keywords, String imageUrl, String authorId, String author, String profileImageUrl) {
        this.caption = caption;
        this.description = description;
        this.keywords = keywords;
        this.imageUrl = imageUrl;
        this.authorId = authorId;
        this.author = author;
        this.profileImageUrl = profileImageUrl; // Initialize new field
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
