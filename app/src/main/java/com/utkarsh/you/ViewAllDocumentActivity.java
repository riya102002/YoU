package com.utkarsh.you;

import                                                              androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewAllDocumentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private DatabaseReference databaseReference, databaseReference1;
    String username, name, id;
    private ImageView emptyBoxImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }

        setContentView(R.layout.activity_view_all_document);

        Intent i = getIntent();
        username = i.getStringExtra("Username");
        name = i.getStringExtra("Name");
        id = i.getStringExtra("ID");

        emptyBoxImage = findViewById(R.id.empty_box_image);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference().child("AllDocuments");
        databaseReference1 = FirebaseDatabase.getInstance().getReference().child("users");
        // Retrieve data from Firebase
        retrieveDataFromFirebase();

        ImageView leaderBoardIcon = findViewById(R.id.leaderBoard);
        leaderBoardIcon.setOnClickListener(v -> getTop3UsersByCredits());

        ImageView filterIcon = findViewById(R.id.filter_icon);
        filterIcon.setOnClickListener(v -> showFilterDialog());
    }

    private void getTop3UsersByCredits() {
        databaseReference1.orderByChild("downloadBalance").limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Userr> topUsers = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String username = userSnapshot.child("username").getValue(String.class);
                    int downloadBalance;
                    try{
                        downloadBalance = userSnapshot.child("downloadBalance").getValue(Integer.class);
                    }catch (Exception e){
                        downloadBalance = 0;
                    }
                    topUsers.add(new Userr(userId, username, downloadBalance));
                }

                Collections.reverse(topUsers); // To get the users with the highest credits first

                showLeaderboardDialog(new CreditsLeaderboardAdapter(topUsers, ViewAllDocumentActivity.this));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void showLeaderboardDialog(CreditsLeaderboardAdapter leaderboardAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewAllDocumentActivity.this);
        builder.setTitle("Leaderboard");

        RecyclerView recyclerView = new RecyclerView(ViewAllDocumentActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewAllDocumentActivity.this));
        recyclerView.setAdapter(leaderboardAdapter);

        builder.setView(recyclerView);

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Course Code");

        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        Spinner spinner = view.findViewById(R.id.spinner_course_codes);

        DatabaseReference courseCodesRef = FirebaseDatabase.getInstance().getReference("CoursesOffered");
        courseCodesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> courseCodes = new ArrayList<>();
                courseCodes.add("All Courses");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String courseCode = snapshot.getKey();
                    if (courseCode != null) {
                        courseCodes.add(courseCode);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewAllDocumentActivity.this, android.R.layout.simple_spinner_item, courseCodes);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ViewAllDocumentActivity", "Failed to fetch course codes: " + databaseError.getMessage());
            }
        });

        builder.setView(view);
        builder.setPositiveButton("Filter", (dialog, which) -> {
            String selectedCourse = spinner.getSelectedItem().toString();
            filterDataByCourse(selectedCourse);
        });
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }


    private void filterDataByCourse(String selectedCourse) {
        List<Book> filteredList = new ArrayList<>();
        for (Book book : bookList) {
            if ("All Courses".equals(selectedCourse) || selectedCourse.equals(book.getCourseType())) {
                filteredList.add(book);
            }
        }

        if (filteredList.isEmpty()) {
            // Show empty box image
            recyclerView.setVisibility(View.GONE);
            emptyBoxImage.setVisibility(View.VISIBLE);
            // TODO: Set the visibility of the empty box image view to visible
        } else {
            // Hide empty box image
            recyclerView.setVisibility(View.VISIBLE);
            emptyBoxImage.setVisibility(View.GONE);
            // TODO: Set the visibility of the empty box image view to gone
            bookAdapter = new BookAdapter(filteredList, ViewAllDocumentActivity.this);
            recyclerView.setAdapter(bookAdapter);
            bookAdapter.notifyDataSetChanged();
        }
    }

    private void retrieveDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous data
                bookList.clear();
                // Iterate through each child node
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String bookName = snapshot.getKey();
                    String documentDescription = snapshot.child("documentDescription").getValue(String.class);
                    String authorName = snapshot.child("name").getValue(String.class);
                    String finalAuthorName = authorName.substring(0, authorName.indexOf('_'));
                    String time = snapshot.child("time").getValue(String.class);
                    String size = snapshot.child("size").getValue(Float.class).toString();
                    String userId = snapshot.child("idd").getValue(String.class);
                    String keywords = snapshot.child("documentKeywords").getValue(String.class);
                    String courseCode = snapshot.child("selectedCourseCode").getValue(String.class);

                    // Retrieve profileImageUrl from users node
                    databaseReference1.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String.class);
                                // Add book to list
                                bookList.add(new Book(bookName, documentDescription, finalAuthorName, time, size, profileImageUrl, userId, keywords, id, courseCode));
                                // Notify adapter of data change
                                bookAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                        }
                    });
                }
                // Initialize and set adapter
                bookAdapter = new BookAdapter(bookList, ViewAllDocumentActivity.this);
                recyclerView.setAdapter(bookAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}