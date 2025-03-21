package com.utkarsh.you;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsFragment extends Fragment {

    // Define constants for image selection
    private static final int PICK_IMAGE_REQUEST = 1;

    // Firebase references
    DatabaseReference databaseReference;
    FirebaseStorage storage;
    StorageReference storageReference;
    CardView cardViewForTextView;

    // UI elements
    TextView textViewSave, id, usernamee, textViewCredits;
    EditText namee;
    ImageView profileImage;
    ProgressBar progressBar;

    // Uri to store the selected image URI
    Uri imageUri;

    // Placeholder image resource ID
    int placeholderImageResource = R.drawable.youicon;

    String userId, username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("profileImages");

        // Initialize UI elements
        id = rootView.findViewById(R.id.editTextIdSettings);
        usernamee = rootView.findViewById(R.id.editTextUsernameSettings);
        namee = rootView.findViewById(R.id.editTextNameSettings);
        textViewSave = rootView.findViewById(R.id.textViewSave);
        profileImage = rootView.findViewById(R.id.profileImage);
        textViewCredits = rootView.findViewById(R.id.textViewCredits);
        progressBar = rootView.findViewById(R.id.progressBar);
        cardViewForTextView = rootView.findViewById(R.id.cardViewForTextView);

        // Initially show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Retrieve user information from arguments
        userId = getArguments().getString("ID");
        username = getArguments().getString("Username");

        id.setText(userId);
        usernamee.setText(username);

        // Load user data and hide progress bar when everything is loaded
        loadDataAndHideProgressBar();

        profileImage.setOnClickListener(v -> openFileChooser());

        textViewSave.setOnClickListener(v -> {
            // Update name and profile image URL
            String newName = namee.getText().toString().trim();
            if (!newName.isEmpty()) {
                databaseReference.child(userId).child("name").setValue(newName);
            }
            Toast.makeText(getContext(), "Name Changes Made Successfully!", Toast.LENGTH_SHORT).show();
            uploadImage(userId);
        });

        return rootView;
    }

    private void loadDataAndHideProgressBar() {
        // Load user's profile image
        databaseReference.child(userId).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Profile image URL not found, save the placeholder image
                    uploadPlaceholderImage(userId);
                } else {
                    String profileImageUrl = snapshot.getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        // Load the profile image using Glide
                        Glide.with(requireContext())
                                .load(profileImageUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .into(profileImage);
                    } else {
                        // Profile image URL is empty or null, load placeholder image
                        Glide.with(requireContext())
                                .load(placeholderImageResource)
                                .apply(RequestOptions.circleCropTransform())
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });

        // Load user's name from Firebase Realtime Database
        databaseReference.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                namee.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });

        // Load user's download balance
        databaseReference.child(userId).child("downloadBalance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    int balance = snapshot.getValue(Integer.class);
                    cardViewForTextView.setVisibility(View.VISIBLE);
                    textViewCredits.setText("Download Balance: " + balance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Hide the progress bar after loading all data
        progressBar.setVisibility(View.GONE);
    }

    // Method to open file chooser for image selection
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Method to handle the result of image selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    // Method to upload image to Firebase Storage and update profile image URL in Firebase Realtime Database
    private void uploadImage(String userId) {
        if (imageUri != null) {
            progressBar.setVisibility(View.VISIBLE); // Show ProgressBar

            // Check the size of the selected image
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Initial compression with maximum quality
                byte[] imageData = baos.toByteArray();
                long imageSizeKB = imageData.length / 1024; // Size in KB

                if (imageSizeKB > 50) {
                    // If image size is more than 50KB, adjust quality for better compression
                    int quality = (int) (50.0 / imageSizeKB * 100); // Calculate quality based on desired size
                    baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                    imageData = baos.toByteArray();
                }

                // Continue with the upload process
                uploadImageData(userId, imageData);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to read image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE); // Hide ProgressBar
            }
        }
    }

    private void uploadImageData(String userId, byte[] imageData) {
        StorageReference fileReference = storageReference.child(userId + ".jpg");

        fileReference.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image upload success, get the download URL
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update profile image URL in Firebase Realtime Database
                        String downloadUrl = uri.toString();
                        databaseReference.child(userId).child("profileImageUrl").setValue(downloadUrl);
                        Toast.makeText(getContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Hide ProgressBar
                    });
                })
                .addOnFailureListener(e -> {
                    // Image upload failed
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Hide ProgressBar
                });
    }

    // Method to upload placeholder image to Firebase Storage and update profile image URL in Firebase Realtime Database
    private void uploadPlaceholderImage(String userId) {
        StorageReference fileReference = storageReference.child(userId + ".jpg");

        // Load the placeholder image from resources using Glide
        Glide.with(requireContext())
                .asBitmap()
                .load(placeholderImageResource)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        // Upload the placeholder image to Firebase Storage
                        fileReference.putBytes(data)
                                .addOnSuccessListener(taskSnapshot -> {
                                    // Image upload success, get the download URL
                                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                        // Update profile image URL in Firebase Realtime Database
                                        String downloadUrl = uri.toString();
                                        databaseReference.child(userId).child("profileImageUrl").setValue(downloadUrl);
                                        Toast.makeText(getContext(), "Placeholder image uploaded successfully", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    // Placeholder image upload failed
                                    Toast.makeText(getContext(), "Failed to upload placeholder image", Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle placeholder image load cancellation
                    }
                });
    }
}
