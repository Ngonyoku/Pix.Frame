package com.example.pixFrame.model;

public class Photos {
    private String imageUrl, title;

    public Photos() {
        //The Empty Constructor is Required in Order for the Class to work with Firebase.
    }

    public Photos(String title, String imageUrl) {
        this.imageUrl = imageUrl;
        if (title.trim().equals("")) {
            title = "My Photo";
        }
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
