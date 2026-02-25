package com.farmo.activities.commonActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.farmo.R;
import com.farmo.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    private static final String TAG         = "ReviewActivity";
    private static final int    MAX_REVIEWS = 15;  // hard cap from JSON
    private static final int    BATCH_SIZE  = 5;   // how many to show per load

    // ── Header ────────────────────────────────────────────────────────────────────
    private ImageView ivProfileImage;
    private TextView  tvFullName;
    private TextView  tvUserId;
    private TextView  tvUserType;
    private RatingBar rbProfileRating;
    private TextView  tvRatingValue;
    private TextView  tvRatingCount;

    // ── Buttons ───────────────────────────────────────────────────────────────────
    private Button btnAddProduct;
    private Button btnWriteReview;

    // ── Summary ───────────────────────────────────────────────────────────────────
    private TextView  tvAvgRating;
    private RatingBar rbAvgRating;
    private TextView  tvTotalReviews;

    // ── Review List ───────────────────────────────────────────────────────────────
    private LinearLayout reviewListContainer;
    private TextView     tvNoReviews;
    private Button       btnLoadMore;
    private TextView     tvLoadMoreInfo;

    // ── Data ──────────────────────────────────────────────────────────────────────
    private List<ReviewItem> allReviews  = new ArrayList<>();
    private int              loadedCount = 0;

    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        initViews();

        UserProfile profile = loadProfileFromJson();
        allReviews = loadReviewsFromJson();

        if (profile != null) {
            bindProfileHeader(profile);
            configureButtons(profile);
        }

        bindReviewSummary(allReviews);

        // Show first batch on open
        loadedCount = 0;
        loadNextBatch();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────────────────────

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ivProfileImage  = findViewById(R.id.ivProfileImage);
        tvFullName      = findViewById(R.id.tvFullName);
        tvUserId        = findViewById(R.id.tvUserId);
        tvUserType      = findViewById(R.id.tvUserType);
        rbProfileRating = findViewById(R.id.rbProfileRating);
        tvRatingValue   = findViewById(R.id.tvRatingValue);
        tvRatingCount   = findViewById(R.id.tvRatingCount);

        btnAddProduct   = findViewById(R.id.btnAddProduct);
        btnWriteReview  = findViewById(R.id.btnWriteReview);

        tvAvgRating     = findViewById(R.id.tvAvgRating);
        rbAvgRating     = findViewById(R.id.rbAvgRating);
        tvTotalReviews  = findViewById(R.id.tvTotalReviews);

        reviewListContainer = findViewById(R.id.reviewListContainer);
        tvNoReviews         = findViewById(R.id.tvNoReviews);
        btnLoadMore         = findViewById(R.id.btnLoadMore);
        tvLoadMoreInfo      = findViewById(R.id.tvLoadMoreInfo);

        btnLoadMore.setOnClickListener(v -> loadNextBatch());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Load profile — assets/farmer_profile.json
    // ─────────────────────────────────────────────────────────────────────────────

    private UserProfile loadProfileFromJson() {
        try {
            InputStream is = getAssets().open("farmer_profile.json");
            byte[] buffer  = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();

            JSONObject obj = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            UserProfile p  = new UserProfile();
            p.setFullName(obj.optString("fullName", ""));
            p.setUserId(obj.optString("userId", ""));
            p.setUserType(obj.optString("userType", ""));
            p.setProfileImageUrl(obj.optString("profileImageUrl", ""));
            p.setVerified(obj.optBoolean("isVerified", false));
            return p;

        } catch (Exception e) {
            Log.e(TAG, "Profile load error: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Load ALL reviews — assets/reviews.json (max 15)
    // ─────────────────────────────────────────────────────────────────────────────

    private List<ReviewItem> loadReviewsFromJson() {
        List<ReviewItem> list = new ArrayList<>();
        try {
            InputStream is = getAssets().open("reviews.json");
            byte[] buffer  = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();

            JSONArray arr = new JSONArray(new String(buffer, StandardCharsets.UTF_8));
            int count     = Math.min(arr.length(), MAX_REVIEWS);

            for (int i = 0; i < count; i++) {
                JSONObject o = arr.getJSONObject(i);
                ReviewItem r = new ReviewItem();
                r.reviewerName   = o.optString("reviewerName",   "Anonymous");
                r.reviewerType   = o.optString("reviewerType",   "Consumer");
                r.reviewerAvatar = o.optString("reviewerAvatar", "");
                r.rating         = (float) o.optDouble("rating", 3.0);
                r.comment        = o.optString("comment",        "");
                r.date           = o.optString("date",           "");
                list.add(r);
            }

        } catch (Exception e) {
            Log.e(TAG, "Reviews load error: " + e.getMessage());
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Load next BATCH_SIZE reviews and append to list
    // ─────────────────────────────────────────────────────────────────────────────

    private void loadNextBatch() {
        if (allReviews.isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
            btnLoadMore.setVisibility(View.GONE);
            tvLoadMoreInfo.setVisibility(View.GONE);
            return;
        }

        tvNoReviews.setVisibility(View.GONE);

        int from = loadedCount;
        int to   = Math.min(loadedCount + BATCH_SIZE, allReviews.size());

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = from; i < to; i++) {
            ReviewItem review = allReviews.get(i);
            View card = inflater.inflate(R.layout.item_review, reviewListContainer, false);

            TextView  tvName    = card.findViewById(R.id.tvReviewerName);
            TextView  tvType    = card.findViewById(R.id.tvReviewerType);
            TextView  tvDate    = card.findViewById(R.id.tvReviewDate);
            RatingBar rbReview  = card.findViewById(R.id.rbReview);
            TextView  tvComment = card.findViewById(R.id.tvReviewComment);
            TextView  tvAvatar  = card.findViewById(R.id.tvReviewerAvatar);

            tvName.setText(review.reviewerName);
            tvType.setText(review.reviewerType);
            tvDate.setText(review.date);
            rbReview.setRating(review.rating);
            tvComment.setText(isEmpty(review.comment) ? "No comment provided." : review.comment);
            tvAvatar.setText(!isEmpty(review.reviewerName)
                    ? String.valueOf(review.reviewerName.charAt(0)).toUpperCase()
                    : "?");

            reviewListContainer.addView(card);
        }

        loadedCount = to;

        // Update "Showing X of Y" label
        tvLoadMoreInfo.setVisibility(View.VISIBLE);
        tvLoadMoreInfo.setText("Showing " + loadedCount + " of " + allReviews.size() + " reviews");

        // Show or hide Load More button — always just says "Load More"
        if (loadedCount >= allReviews.size()) {
            btnLoadMore.setVisibility(View.GONE);
            tvLoadMoreInfo.setText("All " + allReviews.size() + " reviews loaded");
        } else {
            btnLoadMore.setVisibility(View.VISIBLE);
            btnLoadMore.setText("Load More");   // ← simple label, no extra count text
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Bind profile header
    // ─────────────────────────────────────────────────────────────────────────────

    private void bindProfileHeader(UserProfile p) {
        tvFullName.setText(isEmpty(p.getFullName()) ? "Unknown" : p.getFullName());
        tvUserId.setText(isEmpty(p.getUserId()) ? "—" : p.getUserId());
        tvUserType.setText(isEmpty(p.getUserType()) ? "—" : p.getUserType());
        ivProfileImage.setImageResource(R.drawable.vegetables);

        if (p.isVerified()) {
            tvUserType.setText(p.getUserType() + "  ✔ Verified");
            tvUserType.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else {
            tvUserType.setTextColor(getResources().getColor(android.R.color.black, null));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Configure buttons
    // ─────────────────────────────────────────────────────────────────────────────

    private void configureButtons(UserProfile p) {
        String  userType   = p.getUserType() == null ? "" : p.getUserType().toLowerCase().trim();
        boolean isFarmer   = userType.contains("farmer");
        boolean isVerified = p.isVerified();

        if (isFarmer && isVerified) {
            btnAddProduct.setVisibility(View.VISIBLE);
            btnAddProduct.setOnClickListener(v ->
                    Toast.makeText(this, "Add Product clicked", Toast.LENGTH_SHORT).show());
        } else {
            btnAddProduct.setVisibility(View.GONE);
        }

        btnWriteReview.setOnClickListener(v ->
                Toast.makeText(this, "Write Review — coming soon", Toast.LENGTH_SHORT).show());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Bind rating summary
    // ─────────────────────────────────────────────────────────────────────────────

    private void bindReviewSummary(List<ReviewItem> reviews) {
        if (reviews.isEmpty()) {
            tvAvgRating.setText("0.0");
            rbAvgRating.setRating(0f);
            tvTotalReviews.setText("No reviews yet");
            rbProfileRating.setRating(0f);
            tvRatingValue.setText("0.0");
            tvRatingCount.setText("(0)");
            return;
        }

        float sum = 0f;
        for (ReviewItem r : reviews) sum += r.rating;
        float avg = sum / reviews.size();

        tvAvgRating.setText(String.format("%.1f", avg));
        rbAvgRating.setRating(avg);
        tvTotalReviews.setText(reviews.size() + " review" + (reviews.size() > 1 ? "s" : ""));
        rbProfileRating.setRating(avg);
        tvRatingValue.setText(String.format("%.1f", avg));
        tvRatingCount.setText("(" + reviews.size() + ")");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────────

    private boolean isEmpty(String v) {
        return v == null || v.trim().isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Inner model
    // ─────────────────────────────────────────────────────────────────────────────

    public static class ReviewItem {
        public String reviewerName;
        public String reviewerType;
        public String reviewerAvatar;
        public float  rating;
        public String comment;
        public String date;
    }
}