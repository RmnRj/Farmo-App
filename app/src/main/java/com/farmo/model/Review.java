package com.farmo.model;

import com.google.gson.annotations.SerializedName;

public class Review {

    @SerializedName("reviewer_name")
    private String reviewerName;

    @SerializedName("rating")
    private float rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("date")
    private String date;

    public String getReviewerName() { return reviewerName; }
    public float  getRating()       { return rating; }
    public String getComment()      { return comment; }
    public String getDate()         { return date; }
}