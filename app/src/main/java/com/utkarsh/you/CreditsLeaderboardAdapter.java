package com.utkarsh.you;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CreditsLeaderboardAdapter extends RecyclerView.Adapter<CreditsLeaderboardAdapter.ViewHolder> {

    private List<Userr> userList;
    private Context context;

    public CreditsLeaderboardAdapter(List<Userr> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Userr user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView rankTextView, usernameTextView, creditsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            creditsTextView = itemView.findViewById(R.id.creditsTextView);
        }

        public void bind(Userr user) {
            rankTextView.setText(String.valueOf(getAdapterPosition() + 1));
            usernameTextView.setText(user.getUsername());
            creditsTextView.setText(String.valueOf(user.getDownloadBalance()));
        }
    }
}
class Userr {
    private String userId;
    private String username;
    private int downloadBalance;

    public Userr(String userId, String username, int downloadBalance) {
        this.userId = userId;
        this.username = username;
        this.downloadBalance = downloadBalance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getDownloadBalance() {
        return downloadBalance;
    }

    public void setDownloadBalance(int downloadBalance) {
        this.downloadBalance = downloadBalance;
    }
}
