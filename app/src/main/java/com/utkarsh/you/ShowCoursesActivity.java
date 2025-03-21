package com.utkarsh.you;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowCoursesActivity extends AppCompatActivity {

    private EditText editTextCourseCode;
    private Button buttonAddCourse;
    private RecyclerView recyclerViewCourses;
    private CourseAdapter courseAdapter;
    private ArrayList<String> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_courses);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("CoursesOffered").child("Default");
        databaseReference1.setValue(true);

        editTextCourseCode = findViewById(R.id.editTextCourseCode);
        buttonAddCourse = findViewById(R.id.buttonAddCourse);
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses);

        // Initialize RecyclerView
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));
        courseList = new ArrayList<>();
        courseAdapter = new CourseAdapter(courseList);
        recyclerViewCourses.setAdapter(courseAdapter);

        // Button click listener to add course code to Firebase
        buttonAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCourseToFirebase();
            }
        });

        // Retrieve course list from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CoursesOffered");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                courseList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String courseCode = snapshot.getKey();
                    courseList.add(courseCode);
                }
                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ShowCoursesActivity.this, "Failed to retrieve courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCourseToFirebase() {
        String courseCode = editTextCourseCode.getText().toString().trim();
        if (!courseCode.isEmpty()) {
            // Adding course code to Firebase Realtime Database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CoursesOffered");
            databaseReference.child(courseCode).setValue(true); // You can set any value here, I'm using true for simplicity
            Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
            editTextCourseCode.setText(""); // Clearing the EditText after adding the course
        } else {
            Toast.makeText(this, "Please enter a course code", Toast.LENGTH_SHORT).show();
        }
    }
}
