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

import org.w3c.dom.Text;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;

    public BookAdapter(List<Book> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.titleTextView.setText(book.getTitle());
        holder.descriptionTextView.setText(book.getDescription());
        holder.authorTextView.setText("Uploaded By: " + book.getAuthor());
        Double bookSize = Double.parseDouble(book.getSize());
        holder.size.setText((bookSize / 1000) + "KB");
        holder.time.setText("Uploaded At: " + book.getTime());
        holder.courseType.setText("Course Type: " + book.getCourseType());
        // Load profile image using Glide library
        Glide.with(holder.itemView.getContext())
                .load(book.getProfileImageUrl()) // Load image URL from Book object
                .into(holder.profileViewInDoc);

        // Set OnClickListener to the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve verification status from Firebase Realtime Database
                DatabaseReference documentRef = FirebaseDatabase.getInstance().getReference()
                        .child("Documents").child(book.getAuthor()).child(book.getTitle()).child("verified");
                documentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getValue(String.class).equals("true")) {
                            // Redirect to DownloadDocumentActivity since document is verified
                            Intent intent = new Intent(context, DownloadDocumentActivity.class);
                            intent.putExtra("bookName", book.getTitle());
                            intent.putExtra("documentDescription", book.getDescription());
                            intent.putExtra("finalAuthorName", book.getAuthor());
                            intent.putExtra("userId", book.getUserId());
                            intent.putExtra("size", book.getSize());
                            intent.putExtra("time", book.getTime());
                            intent.putExtra("keywords", book.getKeywords());
                            intent.putExtra("currentUser", book.getCurrentUser());
                            intent.putExtra("courseType", book.getCourseType());
                            context.startActivity(intent);
                        } else {
                            // Display a toast indicating the document is yet to be verified
                            Toast.makeText(context, "Document Yet to be verified by Admin!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(context, "Error retrieving document verification status", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView authorTextView;
        TextView size;
        TextView time;
        ImageView profileViewInDoc;
        String userId;
        String keywords;
        String currentUser;
        TextView courseType;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            size = itemView.findViewById(R.id.sizeTextView);
            time = itemView.findViewById(R.id.timeTextView);
            profileViewInDoc = itemView.findViewById(R.id.profileViewInDoc);
            courseType = itemView.findViewById(R.id.courseTypeTextView);
        }
    }
}
class Book {
    private String title;
    private String description;
    private String author;
    private String time;
    private String size;
    private String profileImageUrl;
    private String userId;
    private String keywords;
    private String currentUser;
    private String courseType;

    public Book(String title, String description, String author, String time, String size, String profileImageUrl, String userId, String keywords, String currentUser, String courseType) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.size = size;
        this.time = time;
        this.profileImageUrl = profileImageUrl;
        this.userId = userId;
        this.keywords = keywords;
        this.currentUser = currentUser;
        this.courseType = courseType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getTime(){ return time;}

    public String getSize(){ return size;}

    public String getProfileImageUrl(){ return profileImageUrl;}

    public String getUserId(){return userId;}

    public String getKeywords(){return keywords;}

    public String getCurrentUser(){return currentUser;}

    public String getCourseType(){ return courseType;}

}