package com.utkarsh.you;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<Userrs> userList;

    public UserAdapter(Context context, List<Userrs> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Userrs userrs = userList.get(position);
        if(userrs.getUsername().equalsIgnoreCase(userrs.getMyUsername())){
            holder.username.setText(userrs.getUsername()+" (Me)");
        }
        else{
            holder.username.setText(userrs.getUsername());
        }
        Glide.with(context)
                .load(userrs.getProfileImageUrl())
                .placeholder(R.drawable.youicon) // Optional placeholder image
                .error(R.drawable.youicon) // Optional error image
                .into(holder.profileImage);

        // Set OnClickListener on the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ChatActivity and pass data as extras
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userId", userrs.getUserId());
                intent.putExtra("username", userrs.getUsername());
                intent.putExtra("profileImageUrl", userrs.getProfileImageUrl());
                intent.putExtra("myUserid", userrs.getMyUserid());
                intent.putExtra("myUsername", userrs.getMyUsername());
                intent.putExtra("myProfileimageurl", userrs.getMyProfileimageurl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView username;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
        }
    }
}


class Userrs {
    private String userId;
    private String username;
    private String profileImageUrl;
    private String myUsername;
    private String myUserid;
    private String myProfileimageurl;

    public Userrs() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Userrs(String userId, String username, String profileImageUrl, String myUsername, String myUserid, String myProfileimageurl) {
        this.userId = userId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.myUsername = myUsername;
        this.myUserid = myUserid;
        this.myProfileimageurl = myProfileimageurl;
    }

    public String getMyProfileimageurl() {
        return myProfileimageurl;
    }

    public void setMyProfileimageurl(String myProfileimageurl) {
        this.myProfileimageurl = myProfileimageurl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMyUsername() {
        return myUsername;
    }

    public void setMyUsername(String myUsername) {
        this.myUsername = myUsername;
    }

    public String getMyUserid() {
        return myUserid;
    }

    public void setMyUserid(String myUserid) {
        this.myUserid = myUserid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
