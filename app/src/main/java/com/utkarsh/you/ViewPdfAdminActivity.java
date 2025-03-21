package com.utkarsh.you;

import android.app.ProgressDialog;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewPdfAdminActivity extends AppCompatActivity {

    PDFView pdfView;
    private ImageView verifiedIcon;
    private ImageView notVerifiedIcon;
    String documentName;
    String idd;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_pdf_admin);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }

        pdfView = findViewById(R.id.pdfView);
        verifiedIcon = findViewById(R.id.verifiedIcon);
        notVerifiedIcon = findViewById(R.id.notVerifiedIcon);
        documentName = getIntent().getStringExtra("documentName");
        String documentDescription = getIntent().getStringExtra("documentDescription");
        String documentKeywords = getIntent().getStringExtra("documentKeywords");
        String documentTitle = getIntent().getStringExtra("documentTitle");
        String downloadUrl = getIntent().getStringExtra("downloadUrl");
        idd = getIntent().getStringExtra("idd");
        String selectedCourseCode = getIntent().getStringExtra("selectedCourseCode");
        long size = getIntent().getLongExtra("size", 0); // 0 is the default value
        String time = getIntent().getStringExtra("time");
        String verified = getIntent().getStringExtra("verified"); // false is the default value

        if (verified.equals("false")) {
            verifiedIcon.setVisibility(View.VISIBLE);
            notVerifiedIcon.setVisibility(View.GONE);
        } else {
            notVerifiedIcon.setVisibility(View.VISIBLE);
            verifiedIcon.setVisibility(View.GONE);
        }

        TextView nameTextView = findViewById(R.id.nameTextView);

        verifiedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVerifiedStatus();
            }
        });
        notVerifiedIcon.setOnClickListener(v->setNotVerifiedStatus());
        nameTextView.setText(documentName);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading PDF...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        showPdf(downloadUrl);
    }

    private void showPdf(String link){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(link)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                progressDialog.dismiss();
                Toast.makeText(ViewPdfAdminActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    pdfView.fromStream(inputStream)
                            .onLoad(nbPages -> {
                            }).load();
                });
            }
        });
    }

    private void setVerifiedStatus() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("AllDocuments")
                .child(documentName)
                .child("verified").setValue("true");

        databaseReference.child("Documents")
                .child(documentName.substring(0,documentName.indexOf('_')))
                .child(documentName)
                .child("verified").setValue("true");

        verifiedIcon.setVisibility(View.GONE);
        notVerifiedIcon.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Verification Status set to Verified", Toast.LENGTH_SHORT).show();

        // Increase user's credits by 5
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(idd)
                .child("downloadBalance");

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int currentBalance = dataSnapshot.getValue(Integer.class);
                int newBalance = currentBalance + 5;
                userReference.setValue(newBalance);
                Toast.makeText(ViewPdfAdminActivity.this, "Credits increased by 5", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewPdfAdminActivity.this, "Failed to update credits", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setNotVerifiedStatus() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("AllDocuments")
                .child(documentName)
                .child("verified").setValue("false");

        databaseReference.child("Documents")
                .child(documentName.substring(0,documentName.indexOf('_')))
                .child(documentName)
                .child("verified").setValue("false");

        verifiedIcon.setVisibility(View.VISIBLE);
        notVerifiedIcon.setVisibility(View.GONE);
        Toast.makeText(this, "Verification Status set to Not Verified", Toast.LENGTH_SHORT).show();

        // Increase user's credits by 5
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(idd)
                .child("downloadBalance");

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int currentBalance = dataSnapshot.getValue(Integer.class);
                int newBalance = currentBalance - 5;
                userReference.setValue(newBalance);
                Toast.makeText(ViewPdfAdminActivity.this, "Credits increased by 5", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewPdfAdminActivity.this, "Failed to update credits", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
