package com.utkarsh.you;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CustomDocumentAdapter extends RecyclerView.Adapter<CustomDocumentAdapter.DocumentViewHolder> {

    private ArrayList<CustomDocument> documents = new ArrayList<>();
    private Context context; // Add Context field

    public CustomDocumentAdapter(Context context) {
        this.context = context;
    }

    public void setData(ArrayList<CustomDocument> documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_card, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        CustomDocument document = documents.get(position);
        holder.bind(document);

        // Set onClickListener to handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the information of the clicked item to ViewPdfAdminActivity
                Intent intent = new Intent(context, ViewPdfAdminActivity.class);
                intent.putExtra("documentName", document.getName());
                intent.putExtra("documentDescription", document.getDocumentDescription());
                intent.putExtra("documentKeywords", document.getDocumentKeywords());
                intent.putExtra("documentTitle", document.getDocumentTitle());
                intent.putExtra("downloadUrl", document.getDownloadUrl());
                intent.putExtra("idd", document.getIdd());
                intent.putExtra("selectedCourseCode", document.getSelectedCourseCode());
                intent.putExtra("size", document.getSize());
                intent.putExtra("time", document.getTime());
                intent.putExtra("verified", document.isVerified());
                context.startActivity(intent);
            }
        });

        // Set background color based on verification status
        if (document.isVerified().equals("true")) {
            holder.itemView.setBackgroundResource(R.color.green); // Set green background
        } else {
            holder.itemView.setBackgroundResource(R.color.red); // Set red background
        }
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView descriptionTextView;
        TextView keywordsTextView;
        TextView titleTextView;
//        TextView downloadTextView;
        TextView iddTextView;
        TextView selectedCourseTextView;
        TextView sizeTextView;
        TextView timeTextView;
        TextView verifiedTextView;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            keywordsTextView = itemView.findViewById(R.id.keywordsTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
//            downloadTextView = itemView.findViewById(R.id.downloadTextView);
            iddTextView = itemView.findViewById(R.id.iddTextView);
            selectedCourseTextView = itemView.findViewById(R.id.selectedCourseTextView);
            sizeTextView = itemView.findViewById(R.id.sizeTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            verifiedTextView = itemView.findViewById(R.id.verifiedTextView);
        }

        void bind(CustomDocument document) {
            nameTextView.setText("Doc Name: " + document.getName());
            descriptionTextView.setText("Doc Desc: " + document.getDocumentDescription());
            keywordsTextView.setText("Keywords: "+document.getDocumentKeywords());
            titleTextView.setText("Doc Title: " + document.getDocumentTitle());
//            downloadTextView.setText(document.getDownloadUrl());
            iddTextView.setText("Author ID: "+document.getIdd());
            selectedCourseTextView.setText("Course Code: " + document.getSelectedCourseCode());
            sizeTextView.setText("Size: "+(document.getSize()/1000)+"KB"); // Convert size to String
            timeTextView.setText("Time: "+document.getTime());
            verifiedTextView.setText("Verification Status: "+(document.isVerified()));
        }
    }
}

class CustomDocument {
    private String documentDescription;
    private String documentKeywords;
    private String documentTitle;
    private String downloadUrl;
    private String idd;
    private String name;
    private String selectedCourseCode;
    private long size;
    private String time;
    private String verified;

    public CustomDocument() {
    }

    public CustomDocument(String documentDescription, String documentKeywords, String documentTitle, String downloadUrl, String idd, String name, String selectedCourseCode, long size, String time, String verified) {
        this.documentDescription = documentDescription;
        this.documentKeywords = documentKeywords;
        this.documentTitle = documentTitle;
        this.downloadUrl = downloadUrl;
        this.idd = idd;
        this.name = name;
        this.selectedCourseCode = selectedCourseCode;
        this.size = size;
        this.time = time;
        this.verified = verified;
    }

    public String getDocumentDescription() {
        return documentDescription;
    }

    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    public String getDocumentKeywords() {
        return documentKeywords;
    }

    public void setDocumentKeywords(String documentKeywords) {
        this.documentKeywords = documentKeywords;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getIdd() {
        return idd;
    }

    public void setIdd(String idd) {
        this.idd = idd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelectedCourseCode() {
        return selectedCourseCode;
    }

    public void setSelectedCourseCode(String selectedCourseCode) {
        this.selectedCourseCode = selectedCourseCode;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String isVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    // Add constructors, getters, and setters as needed
}
