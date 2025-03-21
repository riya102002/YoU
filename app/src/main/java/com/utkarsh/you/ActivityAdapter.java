// ActivityAdapter.java
package com.utkarsh.you;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private Context context;
    private List<ActivityItem> itemList;

    public ActivityAdapter(Context context, List<ActivityItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity_card, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem item = itemList.get(position);
        holder.usernameTextView.setText(item.getUserName());
        holder.messageTextView.setText(item.getMessage());

        // Load image into the circleImageView if it's a post
        if (item.getItemId().endsWith("PostUpload")) {
            DatabaseReference postReference = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(item.getItemName());
            postReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        Glide.with(context).load(imageUrl).into(holder.circleImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }

        holder.arrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemId = item.getItemId();
                String itemName = item.getItemName();
                if (itemId.endsWith("DocUpload")) {
                    DatabaseReference documentReference = FirebaseDatabase.getInstance().getReference().child("AllDocuments").child(itemName);
                    documentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String verified = dataSnapshot.child("verified").getValue(String.class);
                                if (verified != null && verified.equals("true")) {
                                    // Redirect to DownloadDocumentActivity if document is verified
                                    Intent intent = new Intent(context, DownloadDocumentActivity.class);
                                    // Retrieve data from Firebase Realtime Database and pass it via intent extras
                                    String bookName = itemName;
                                    String documentDescription = dataSnapshot.child("documentDescription").getValue(String.class);
                                    String authorName = dataSnapshot.child("name").getValue(String.class);
                                    String finalAuthorName = authorName.substring(0, authorName.indexOf('_'));
                                    String docUserId = dataSnapshot.child("idd").getValue(String.class);
                                    long size = dataSnapshot.child("size").getValue(Long.class);
                                    String time = dataSnapshot.child("time").getValue(String.class);
                                    String keywords = dataSnapshot.child("documentKeywords").getValue(String.class);
                                    String courseType = dataSnapshot.child("selectedCourseCode").getValue(String.class);

                                    intent.putExtra("bookName", bookName);
                                    intent.putExtra("documentDescription", documentDescription);
                                    intent.putExtra("finalAuthorName", finalAuthorName);
                                    intent.putExtra("userId", docUserId);
                                    intent.putExtra("size", size + "");
                                    intent.putExtra("time", time);
                                    intent.putExtra("courseType", courseType);
                                    intent.putExtra("keywords", keywords);
                                    intent.putExtra("currentUser", item.getCurrentUserId());

                                    context.startActivity(intent);
                                } else {
                                    // Display a toast indicating that the document is yet to be verified by admin
                                    Toast.makeText(context, "Document Yet to be verified by Admin!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }else{
                    Intent intent = new Intent(context, ViewUserProfileActivity.class);
                    DatabaseReference postReference = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(itemName);
                    postReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String postId = itemName;
                                String authorId = snapshot.child("authorId").getValue(String.class);
                                String authorName = snapshot.child("author").getValue(String.class);
                                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                                intent.putExtra("authorId", authorId);
                                intent.putExtra("authorUsername", authorName);
                                context.startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder class for the RecyclerView adapter
    public class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView messageTextView;
        ImageView arrowImageView;
        ImageView circleImageView;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            arrowImageView = itemView.findViewById(R.id.right_arrow_button);
            circleImageView = itemView.findViewById(R.id.circleImageView);
        }
    }
}

class ActivityItem {
    private String userName;
    private String message;
    private String itemId;
    private String itemName;
    private String currentUserId;

    public ActivityItem(String userName, String message, String itemId, String itemName, String currentUserId) {
        this.userName = userName;
        this.message = message;
        this.itemId = itemId;
        this.itemName = itemName;
        this.currentUserId = currentUserId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public String getItemId() {
        return itemId;
    }
}
