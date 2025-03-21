package com.utkarsh.you;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DownloadDocumentActivity extends AppCompatActivity {

    String bookName, documentDescription, finalAuthorName, userId, size, time, keywords, currentUser, downloadUrl, courseType;
    CardView downloadCard;
    DatabaseReference userRef, documentRef;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_document);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }

        Intent i = getIntent();
        bookName = i.getStringExtra("bookName");
        documentDescription = i.getStringExtra("documentDescription");
        finalAuthorName = i.getStringExtra("finalAuthorName");
        userId = i.getStringExtra("userId");
        size = i.getStringExtra("size");
        time = i.getStringExtra("time");
        keywords = i.getStringExtra("keywords");
        currentUser = i.getStringExtra("currentUser");
        courseType = i.getStringExtra("courseType");

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser);
        documentRef = FirebaseDatabase.getInstance().getReference().child("Documents").child(finalAuthorName).child(bookName);

        // Find TextViews by their IDs
        TextView pdfNameTextView = findViewById(R.id.pdfNameTextView);
        TextView pdfSizeTextView = findViewById(R.id.pdfSizeTextView);
        TextView pdfAuthorTextView = findViewById(R.id.pdfAuthorTextView);
        TextView pdfDescTextView = findViewById(R.id.pdfDescTextView);
        TextView pdfKeywordsTextView = findViewById(R.id.pdfKeywordsTextView);
        TextView courseTypeTextView = findViewById(R.id.courseTypeTextView);
        downloadCard = findViewById(R.id.downloadCard);
        progressBar = findViewById(R.id.progressBar);

        int firstUnderscoreIndex = bookName.indexOf('_');
        int secondUnderscoreIndex = bookName.indexOf('_', firstUnderscoreIndex + 1);

        // Set respective data to TextViews
        pdfNameTextView.setText(bookName.substring(secondUnderscoreIndex + 1, bookName.lastIndexOf('_')));
        pdfSizeTextView.setText(Double.parseDouble(size) / 1000 + "KB");
        pdfAuthorTextView.setText(finalAuthorName);
        pdfDescTextView.setText(documentDescription);
        courseTypeTextView.setText("Course Code: "+courseType);
        // Assuming you have a variable for keywords, replace "keywords" with your actual variable
        pdfKeywordsTextView.setText(keywords);

        // Add OnClickListener to downloadCard
        downloadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve download balance from Firebase Realtime Database
                progressBar.setVisibility(View.VISIBLE);
                userRef.child("downloadBalance").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            long downloadBalance = dataSnapshot.getValue(Long.class);
                            if (downloadBalance >= 1) {
                                // Decrement download balance by 1
                                userRef.child("downloadBalance").setValue(downloadBalance - 1);
                                // Proceed with document download
                                downloadDocument();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(DownloadDocumentActivity.this, "Insufficient download credits", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(DownloadDocumentActivity.this, "Failed to retrieve download balance", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void downloadDocument() {
        documentRef.child("downloadUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    downloadUrl = dataSnapshot.getValue(String.class);
                    if (downloadUrl != null) {
                        // Start download intent
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                        startActivity(intent);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DownloadDocumentActivity.this, "Download URL not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DownloadDocumentActivity.this, "Download URL not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DownloadDocumentActivity.this, "Failed to retrieve download URL", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
