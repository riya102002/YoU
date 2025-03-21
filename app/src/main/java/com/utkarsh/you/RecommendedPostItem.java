package com.utkarsh.you;

public class RecommendedPostItem {
    private String postName;
    private String posttitle;
    private String postdescription;
    private String postimageResource;
    private String authorName;

    public RecommendedPostItem(String postName, String posttitle, String postdescription, String postimageResource, String authorName) {
        this.postName = postName;
        this.posttitle = posttitle;
        this.postdescription = postdescription;
        this.postimageResource = postimageResource;
        this.authorName = authorName;
    }

    public String getPostName(){
        return postName;
    }

    public String getPosttitle() {
        return posttitle;
    }

    public String getPostdescription() {
        return postdescription;
    }

    public String getPostimageResource() {
        return postimageResource;
    }

    public String getAuthorName(){ return authorName; }
}
