package com.utkarsh.you;

public class RecommendedItem {
    private String documentName;
    private String title;
    private String description;
    private int imageResource;
    private String courseType;

    public RecommendedItem(String documentName, String title, String description, int imageResource, String courseType) {
        this.documentName = documentName;
        this.title = title;
        this.description = description;
        this.imageResource = imageResource;
        this.courseType = courseType;
    }

    public String getDocumentName(){
        return documentName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getCourseType(){ return courseType;}
}
