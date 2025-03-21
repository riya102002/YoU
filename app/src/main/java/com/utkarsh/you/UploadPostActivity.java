package com.utkarsh.you;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class UploadPostActivity extends AppCompatActivity {
    private String username, id, postID;
    private ImageView imagePreviewIcon;
    private TextView selectedPdfName;
    private Uri selectedImageUri;

    private StorageReference storageReference;
    private DatabaseReference databaseReference, mDatabaseRefAllPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FCF9C1"));
        }

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        mDatabaseRefAllPost = FirebaseDatabase.getInstance().getReference("AllPosts");

        Intent intent = getIntent();
        username = intent.getStringExtra("Username");
        id = intent.getStringExtra("ID");

        imagePreviewIcon = findViewById(R.id.image_preview_icon);
        selectedPdfName = findViewById(R.id.selected_pdf_name);

        ImageView uploadIcon = findViewById(R.id.upload_icon);
        uploadIcon.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, 1);
        });

        Button uploadPostButton = findViewById(R.id.upload_post_button);
        uploadPostButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                String postID = generatePostID();
                if (postID != null) {
                    uploadImage(selectedImageUri, postID);
                } else {
                    Toast.makeText(UploadPostActivity.this, "Failed to generate post ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                String postID = generatePostID();
                if (postID != null) {
                    uploadImage(null, postID); // Passing null as imageUri to indicate no image selected
                } else {
                    Toast.makeText(UploadPostActivity.this, "Failed to generate post ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImage(Uri imageUri, String postID) {
        if (imageUri != null) {
            try {
                // Get the bitmap from the imageUri
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // Check if the size is greater than 50kb
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                int lengthInKb = byteArray.length / 1024;
                byte[] data = new byte[0];
                if (lengthInKb > 50) {
                    // Resize the bitmap to reduce its size
                    double quality = 50.0 / lengthInKb * 100;
//                    // Convert the bitmap to bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, (int) quality, baos);
                data = baos.toByteArray();
                }



                // Create a StorageReference with the desired path
                StorageReference imageRef = storageReference.child("Posts").child(username).child(postID + ".jpeg");

                // Upload the image to the specified path
                UploadTask uploadTask = imageRef.putBytes(data);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        savePostDetails(imageUrl, postID);
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(UploadPostActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(UploadPostActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        } else {
            // If no image is selected, upload a placeholder image
            Drawable drawable = getResources().getDrawable(R.drawable.imageplaceholder);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Create a StorageReference for the placeholder image
            StorageReference placeholderRef = storageReference.child("Posts").child(username).child(postID + ".jpeg");

            // Upload the placeholder image to the specified path
            UploadTask placeholderUploadTask = placeholderRef.putBytes(data);

            placeholderUploadTask.addOnSuccessListener(taskSnapshot -> {
                placeholderRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    savePostDetails(imageUrl, postID);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(UploadPostActivity.this, "Failed to upload placeholder image", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void savePostDetails(String imageUrl, String postID) {
        String caption = ((TextView) findViewById(R.id.editTextPostCaption)).getText().toString();
        String description = ((TextView) findViewById(R.id.editTextPostDescription)).getText().toString();
        String keywords = ((TextView) findViewById(R.id.editTextPostKeywords)).getText().toString();

        Map<String, Object> postDetails = new HashMap<>();
        postDetails.put("imageUrl", imageUrl);
        postDetails.put("caption", caption);
        postDetails.put("description", description);
        postDetails.put("keywords", keywords);
        postDetails.put("author", username);
        postDetails.put("authorId", id);

        databaseReference.child(username).child(postID).setValue(postDetails)
                .addOnSuccessListener(aVoid -> {
                    mDatabaseRefAllPost.child(postID).setValue(postDetails);
                    updateLatestPost(postID);

                    // Store post information under Activities node
                    String currentDateAndTime = getCurrentDateAndTime();
                    String typePost = "PostUpload";

                    DatabaseReference activitiesRef = FirebaseDatabase.getInstance().getReference("Activities")
                            .child(username + "_" + currentDateAndTime + "_" + typePost)
                            .child(username)
                            .child(postID);
                    activitiesRef.setValue(postDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("UploadPostActivity", "Post activity saved successfully");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("UploadPostActivity", "Failed to save post activity: " + e.getMessage());
                        }
                    });

                    Toast.makeText(UploadPostActivity.this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(UploadPostActivity.this, "Failed to save post details", Toast.LENGTH_SHORT).show());
    }

    private String generatePostID() {
        return UUID.randomUUID().toString();
    }

    private String getCurrentDateAndTime() {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void updateLatestPost(String postID) {
        DatabaseReference latestPostsRef = FirebaseDatabase.getInstance().getReference("latestPosts");

        latestPostsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Get the current list of latest posts
                List<String> latestPosts = new ArrayList<>();
                if (mutableData.getValue() != null) {
                    for (MutableData post : mutableData.getChildren()) {
                        latestPosts.add(post.getValue(String.class));
                    }
                }

                // Add the new post to the top of the list
                latestPosts.add(0, postID);

                // If the list exceeds 5 posts, remove the oldest post
                if (latestPosts.size() > 5) {
                    latestPosts.remove(5);
                }

                // Update the latestPosts node with the updated list
                mutableData.setValue(latestPosts);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("UploadPostActivity", "Failed to update latest posts: " + databaseError.getMessage());
                } else {
                    Log.d("UploadPostActivity", "Latest posts updated successfully");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreviewIcon.setImageURI(selectedImageUri);
            selectedPdfName.setText("File Selected");
        }
    }
}