package com.utkarsh.you;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UploadDocumentActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private Uri filePath;
    private String username, id, name;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef, mDatabaseRefAllDoc;
    private ProgressDialog progressDialog;
    private Spinner courseCodeSpinner;
    private ArrayAdapter<CharSequence> spinnerAdapter;

    // Views for PDF preview and filename display
    private ImageView pdfPreviewIcon;
    private TextView selectedPdfName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_document);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FCF9C1"));
        }

        Intent i = getIntent();
        username = i.getStringExtra("Username");
        name = i.getStringExtra("Name");
        id = i.getStringExtra("ID");

        courseCodeSpinner = findViewById(R.id.course_code_spinner);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Documents");
        mDatabaseRefAllDoc = FirebaseDatabase.getInstance().getReference("AllDocuments");

        pdfPreviewIcon = findViewById(R.id.pdf_preview_icon);
        selectedPdfName = findViewById(R.id.selected_pdf_name);

        TextView chooseFileText = findViewById(R.id.choose_file_text);
        ImageView uploadIcon = findViewById(R.id.upload_icon);
        Button uploadButton = findViewById(R.id.upload_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        uploadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        chooseFileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null) {
                    String selectedCourseCode = courseCodeSpinner.getSelectedItem().toString();
                    uploadFile(selectedCourseCode);
                } else {
                    Toast.makeText(UploadDocumentActivity.this, "Please choose a file first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePath = null;
                resetPreview();
                Toast.makeText(UploadDocumentActivity.this, "File selection cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch course codes from Firebase Realtime Database
        fetchCourseCodesFromFirebase();
    }

    private void fetchCourseCodesFromFirebase() {
        DatabaseReference courseCodesRef = FirebaseDatabase.getInstance().getReference("CoursesOffered");

        courseCodesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> courseCodes = new ArrayList<>();
                courseCodes.add("Default");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String courseCode = snapshot.getKey();
                    if (courseCode != null) {
                        courseCodes.add(courseCode);
                    }
                }
                populateSpinner(courseCodes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UploadDocumentActivity", "Failed to fetch course codes: " + databaseError.getMessage());
            }
        });
    }

    private void populateSpinner(List<String> courseCodes) {
        List<CharSequence> charSequences = new ArrayList<>();
        for (String courseCode : courseCodes) {
            charSequences.add(courseCode);
        }

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, charSequences);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseCodeSpinner.setAdapter(spinnerAdapter);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            displayPreview();
            Toast.makeText(this, "PDF selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPreview() {
        // Set PDF icon as preview
        pdfPreviewIcon.setImageResource(R.drawable.pdficon);

        // Display selected PDF filename
        String fileName = getFileNameFromUri(filePath);
        selectedPdfName.setText(fileName);
    }

    private void resetPreview() {
        // Clear PDF icon and filename display
        pdfPreviewIcon.setImageResource(0);
        selectedPdfName.setText("No file selected");
    }

    private void uploadFile(String selectedCourseCode) {
        if (filePath != null) {
            progressDialog.show(); // Show progress dialog
            SimpleDateFormat sdf = new SimpleDateFormat("HHmmss", Locale.getDefault());
            String currentTime = sdf.format(new Date());

            // Extracting the original file name from the Uri
            String originalFileName = getFileNameFromUri(filePath);

            // Generating the new file name with username, current time, and original file name
            String fileName = username + "_" + currentTime + "_" + originalFileName;

            // Get the size of the file
            Cursor cursor = getContentResolver().query(filePath, null, null, null, null);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            long fileSize = cursor.getLong(sizeIndex);
            cursor.close();

            // Check if the file size is greater than 0
            if (fileSize > 0) {
                EditText documentTitleEditText = findViewById(R.id.editTextDocumentTitle);
                EditText documentDescriptionEditText = findViewById(R.id.editTextDocumentDescription);
                EditText documentKeywordsEditText = findViewById(R.id.editTextDocumentKeywords);

                String documentTitle = documentTitleEditText.getText().toString();
                String documentDescription = documentDescriptionEditText.getText().toString();
                String documentKeywords = documentKeywordsEditText.getText().toString();

                // Check if any of the fields are empty
                if (documentTitle.isEmpty() || documentDescription.isEmpty() || documentKeywords.isEmpty()) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                StorageReference fileRef = mStorageRef.child("Documents").child(username).child(fileName);

                fileRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss(); // Dismiss progress dialog
                                // Get the download URL from the task snapshot
                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUri) {
                                        // Save document details to Realtime Database
                                        saveDocumentDetailsToDatabase(fileName, documentTitle, documentDescription, documentKeywords, downloadUri.toString(), fileSize, id, selectedCourseCode);
                                        saveDocumentDetailsToDatabaseForAllDoc(fileName, documentTitle, documentDescription, documentKeywords, downloadUri.toString(), fileSize, id, selectedCourseCode);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("UploadDocumentActivity", "Failed to get download URL: " + e.getMessage());
                                    }
                                });

                                Toast.makeText(UploadDocumentActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                                filePath = null;
                                resetPreview();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss(); // Dismiss progress dialog
                                Log.e("UploadDocumentActivity", "Upload failed: " + e.getMessage());
                                Toast.makeText(UploadDocumentActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                progressDialog.dismiss(); // Dismiss progress dialog
                Toast.makeText(this, "Selected PDF is empty", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to extract file name from Uri
    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void saveDocumentDetailsToDatabase(String fileName, String documentTitle, String documentDescription, String documentKeywords, String downloadUrl, long fileSize, String idd, String selectedCourseCode) {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String typeDoc = "DocUpload";
        // Replace '.' with '_' in the filename
        String modifiedFileName = fileName.replace('.', '_');
        String verified = "false";
        Document document = new Document(modifiedFileName, currentTime, documentTitle, documentDescription, documentKeywords, downloadUrl, fileSize, idd, selectedCourseCode, verified);
        String newusername = username.replace('.', '_');
        DatabaseReference userRef = mDatabaseRef.child(newusername).child(modifiedFileName);

        userRef.setValue(document).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Increment downloadBalance by 5
                incrementDownloadBalance(newusername);

                DatabaseReference activitiesRef = FirebaseDatabase.getInstance().getReference("Activities")
                        .child(username + "_" + currentDate + "_" + currentTime + "_" + typeDoc)
                        .child(username)
                        .child(modifiedFileName);
                activitiesRef.setValue(document).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("UploadDocumentActivity", "Document activity saved successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("UploadDocumentActivity", "Failed to save document activity: " + e.getMessage());
                    }
                });

                // Update latestDoc node with the new document
                updateLatestDocument(modifiedFileName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("UploadDocumentActivity", "Failed to save document details: " + e.getMessage());
            }
        });
    }

    private void saveDocumentDetailsToDatabaseForAllDoc(String fileName, String documentTitle, String documentDescription, String documentKeywords, String downloadUrl, long fileSize, String idd, String selectedCourseCode) {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Replace '.' with '_' in the filename
        String modifiedFileName = fileName.replace('.', '_');
        String verified = "false";
        Document document = new Document(modifiedFileName, currentTime, documentTitle, documentDescription, documentKeywords, downloadUrl, fileSize, idd, selectedCourseCode, verified);
        String newusername = username.replace('.', '_');
        DatabaseReference userRef = mDatabaseRefAllDoc.child(modifiedFileName);

        userRef.setValue(document).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("UploadDocumentActivity", "Failed to save document details: " + e.getMessage());
            }
        });
    }

    private void updateLatestDocument(String documentName) {
        DatabaseReference latestDocRef = FirebaseDatabase.getInstance().getReference("latestDoc");

        latestDocRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Get the current list of latest documents
                List<String> latestDocuments = new ArrayList<>();
                if (mutableData.getValue() != null) {
                    for (MutableData doc : mutableData.getChildren()) {
                        latestDocuments.add(doc.getValue(String.class));
                    }
                }

                // Add the new document to the top of the list
                latestDocuments.add(0, documentName);

                // If the list exceeds 5 documents, remove the oldest document
                if (latestDocuments.size() > 5) {
                    latestDocuments.remove(5);
                }

                // Update the latestDoc node with the updated list
                mutableData.setValue(latestDocuments);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("UploadDocumentActivity", "Failed to update latest documents: " + databaseError.getMessage());
                } else {
                    Log.d("UploadDocumentActivity", "Latest documents updated successfully");
                }
            }
        });
    }

    private void incrementDownloadBalance(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(id).child("downloadBalance");

        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Get current downloadBalance
                Integer currentBalance = mutableData.getValue(Integer.class);
                if (currentBalance == null) {
                    // If downloadBalance is null, set it to 0
                    mutableData.setValue(0);
                } else {
                    // Increment downloadBalance by 5
//                    mutableData.setValue(currentBalance + 5);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("UploadDocumentActivity", "Failed to increment downloadBalance: " + databaseError.getMessage());
                } else {
                    Log.d("UploadDocumentActivity", "Download balance incremented by 5");
                }
            }
        });
    }
}
class Document {
    private String name;
    private String time;
    private String documentTitle;
    private String documentDescription;
    private String documentKeywords;
    private String downloadUrl;
    private long size;
    private String idd;
    private String selectedCourseCode;
    private String verified;

    public Document() {
        // Default constructor required for calls to DataSnapshot.getValue(Document.class)
    }

    public Document(String name, String time, String documentTitle, String documentDescription, String documentKeywords, String downloadUrl, long size, String idd, String selectedCourseCode, String verified) {
        this.name = name;
        this.time = time;
        this.documentTitle = documentTitle;
        this.documentDescription = documentDescription;
        this.documentKeywords = documentKeywords;
        this.downloadUrl = downloadUrl;
        this.size = size;
        this.idd = idd;
        this.selectedCourseCode = selectedCourseCode;
        this.verified = verified;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public String getDocumentDescription() {
        return documentDescription;
    }

    public String getDocumentKeywords() {
        return documentKeywords;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public long getSize() {
        return size;
    }

    public String getIdd(){ return idd; }

    public String getSelectedCourseCode(){ return selectedCourseCode;}

    public String getVerified() {
        return verified;
    }
}
