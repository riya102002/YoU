package com.utkarsh.you;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    private List<DocumentItem> documentList;

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, timeTextView, verifiedTextView;
        TextView descriptionTextView, selectedCourseTextView, sizeTextView;
        TextView keywordsTextView, titleTextView, downloadTextView, iddTextView;
        DocumentViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            verifiedTextView = itemView.findViewById(R.id.verifiedTextView);
            selectedCourseTextView = itemView.findViewById(R.id.selectedCourseTextView);
            sizeTextView = itemView.findViewById(R.id.sizeTextView);
            keywordsTextView = itemView.findViewById(R.id.keywordsTextView);
            titleTextView = itemView.findViewById(R.id.titlePostTextView);
//            downloadTextView = itemView.findViewById(R.id.downloadTextView);
            iddTextView = itemView.findViewById(R.id.iddTextView);


        }
    }

    public DocumentAdapter(List<DocumentItem> documentList) {
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_card, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        DocumentItem currentItem = documentList.get(position);
        holder.nameTextView.setText(currentItem.getName());
        holder.descriptionTextView.setText(currentItem.getDocumentDescription());
        holder.iddTextView.setText(currentItem.getIdd());
        holder.titleTextView.setText(currentItem.getDocumentTitle());
        holder.downloadTextView.setText(currentItem.getDownloadUrl());
        holder.keywordsTextView.setText(currentItem.getDocumentKeywords());
        holder.sizeTextView.setText(currentItem.getSize()+"");
        holder.timeTextView.setText(currentItem.getTime()+"");
        holder.selectedCourseTextView.setText(currentItem.getSelectedCourseCode());
        holder.verifiedTextView.setText(currentItem.isVerified()+"");
        // Bind other data to respective TextViews
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }
}
class DocumentItem {
    private String name;
    private String documentDescription;
    private String documentKeywords;
    private String documentTitle;
    private String downloadUrl;
    private String idd;
    private String selectedCourseCode;
    private long size;
    private long time;
    private boolean verified;

    public DocumentItem(){}

    public DocumentItem(String name, String documentDescription, String documentKeywords, String documentTitle, String downloadUrl, String idd, String selectedCourseCode, long size, long time, boolean verified) {
        this.name = name;
        this.documentDescription = documentDescription;
        this.documentKeywords = documentKeywords;
        this.documentTitle = documentTitle;
        this.downloadUrl = downloadUrl;
        this.idd = idd;
        this.selectedCourseCode = selectedCourseCode;
        this.size = size;
        this.time = time;
        this.verified = verified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    // Constructor, getters, and setters
}
