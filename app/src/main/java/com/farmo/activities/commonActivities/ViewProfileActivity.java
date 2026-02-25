package com.farmo.activities.commonActivities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.farmo.R;
import com.farmo.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ViewProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileDetail";

    private ImageView    ivProfileImage;
    private TextView     tvFullName, tvUserId, tvUserType;
    private TextView     tvReviewStars, tvReviewCount;
    private Button       btnSeeReviews;
    private LinearLayout rowMobile, rowEmail, rowWhatsapp, rowFacebook;
    private TextView     tvMobileNumber, tvEmail, tvWhatsapp, tvFacebook;
    private TextView     tvDOB, tvSex;
    private TextView     tvProvince, tvDistrict, tvMunicipality, tvWardNo, tvTole;
    private TextView     tvAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        bindViews();
        loadReviewSummary();

        UserProfile profile = loadProfile();
        if (profile != null) {
            populateUI(profile);
        }
    }

    // ── Bind all views ────────────────────────────────────────────────────────────
    private void bindViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvFullName     = findViewById(R.id.tvFullName);
        tvUserId       = findViewById(R.id.tvUserId);
        tvUserType     = findViewById(R.id.tvUserType);
        tvReviewStars  = findViewById(R.id.tvReviewStars);
        tvReviewCount  = findViewById(R.id.tvReviewCount);
        btnSeeReviews  = findViewById(R.id.btnSeeReviews);

        rowMobile   = findViewById(R.id.rowMobile);
        rowEmail    = findViewById(R.id.rowEmail);
        rowWhatsapp = findViewById(R.id.rowWhatsapp);
        rowFacebook = findViewById(R.id.rowFacebook);

        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        tvEmail        = findViewById(R.id.tvEmail);
        tvWhatsapp     = findViewById(R.id.tvWhatsapp);
        tvFacebook     = findViewById(R.id.tvFacebook);

        tvDOB          = findViewById(R.id.tvDOB);
        tvSex          = findViewById(R.id.tvSex);
        tvProvince     = findViewById(R.id.tvProvince);
        tvDistrict     = findViewById(R.id.tvDistrict);
        tvMunicipality = findViewById(R.id.tvMunicipality);
        tvWardNo       = findViewById(R.id.tvWardNo);
        tvTole         = findViewById(R.id.tvTole);
        tvAbout        = findViewById(R.id.tvAbout);
    }

    // ── Populate UI from profile ──────────────────────────────────────────────────
    private void populateUI(UserProfile p) {

        // Header
        tvFullName.setText(hasValue(p.getFullName()) ? p.getFullName() : "Unknown");
        tvUserId.setText(hasValue(p.getUserId()) ? p.getUserId() : "—");
        tvUserType.setText(hasValue(p.getUserType()) ? p.getUserType() : "—");
        ivProfileImage.setImageResource(R.drawable.vegetables);

        // ── MOBILE ────────────────────────────────────────────────────────────────
        String mobile = p.getMobileNumber();
        if (hasValue(mobile)) {
            tvMobileNumber.setText(mobile);
            tvMobileNumber.setTextColor(getResources().getColor(android.R.color.black));
            // When data exists → clicking opens phone dialer
            rowMobile.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mobile.trim()));
                startActivity(intent);
            });
        } else {
            tvMobileNumber.setText("Empty");
            tvMobileNumber.setTextColor(getResources().getColor(android.R.color.darker_gray));
            // When empty → clicking shows Toast "Empty"
            rowMobile.setOnClickListener(v ->
                    Toast.makeText(this, "Mobile Number is empty", Toast.LENGTH_SHORT).show());
        }

        // ── EMAIL ─────────────────────────────────────────────────────────────────
        String email = p.getEmail();
        if (hasValue(email)) {
            tvEmail.setText(email);
            tvEmail.setTextColor(getResources().getColor(android.R.color.black));
            // When data exists → clicking opens email app
            rowEmail.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email.trim()));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            tvEmail.setText("Empty");
            tvEmail.setTextColor(getResources().getColor(android.R.color.darker_gray));
            rowEmail.setOnClickListener(v ->
                    Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show());
        }

        // ── WHATSAPP ──────────────────────────────────────────────────────────────
        String whatsapp = p.getWhatsapp();
        if (hasValue(whatsapp)) {
            tvWhatsapp.setText(whatsapp);
            tvWhatsapp.setTextColor(getResources().getColor(android.R.color.black));
            // When data exists → clicking opens WhatsApp
            rowWhatsapp.setOnClickListener(v -> {
                String digits = whatsapp.replaceAll("[^0-9]", "");
                boolean installed = isAppInstalled("com.whatsapp");
                if (installed) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setPackage("com.whatsapp");
                        i.setData(Uri.parse("https://wa.me/" + digits));
                        startActivity(i);
                    } catch (Exception e) {
                        openInBrowser("https://wa.me/" + digits);
                    }
                } else {
                    openInBrowser("https://wa.me/" + digits);
                }
            });
        } else {
            tvWhatsapp.setText("Empty");
            tvWhatsapp.setTextColor(getResources().getColor(android.R.color.darker_gray));
            rowWhatsapp.setOnClickListener(v ->
                    Toast.makeText(this, "WhatsApp number is empty", Toast.LENGTH_SHORT).show());
        }

        // ── FACEBOOK ──────────────────────────────────────────────────────────────
        String facebook = p.getFacebook();
        if (hasValue(facebook)) {
            tvFacebook.setText(facebook);
            tvFacebook.setTextColor(getResources().getColor(android.R.color.black));
            // When data exists → clicking opens Facebook
            rowFacebook.setOnClickListener(v -> {
                String url = facebook.trim();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                if (isAppInstalled("com.facebook.katana")) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        i.setPackage("com.facebook.katana");
                        startActivity(i);
                    } catch (Exception e) {
                        openInBrowser(url);
                    }
                } else {
                    openInBrowser(url);
                }
            });
        } else {
            tvFacebook.setText("Empty");
            tvFacebook.setTextColor(getResources().getColor(android.R.color.darker_gray));
            rowFacebook.setOnClickListener(v ->
                    Toast.makeText(this, "Facebook link is empty", Toast.LENGTH_SHORT).show());
        }

        // ── Personal ──────────────────────────────────────────────────────────────
        tvDOB.setText(hasValue(p.getDateOfBirth()) ? p.getDateOfBirth() : "Empty");
        tvSex.setText(hasValue(p.getSex()) ? p.getSex() : "Empty");

        // ── Address ───────────────────────────────────────────────────────────────
        tvProvince.setText(hasValue(p.getProvince()) ? p.getProvince() : "Empty");
        tvDistrict.setText(hasValue(p.getDistrict()) ? p.getDistrict() : "Empty");
        tvMunicipality.setText(hasValue(p.getMunicipality()) ? p.getMunicipality() : "Empty");
        tvWardNo.setText(hasValue(p.getWardNo()) ? p.getWardNo() : "Empty");
        tvTole.setText(hasValue(p.getTole()) ? p.getTole() : "Empty");

        // ── About ─────────────────────────────────────────────────────────────────
        tvAbout.setText(hasValue(p.getAbout()) ? p.getAbout() : "No description available.");
    }

    // ── Load profile from assets/farmer_profile.json ─────────────────────────────
    private UserProfile loadProfile() {
        try {
            InputStream is = getAssets().open("farmer_profile.json");
            byte[] buf = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buf);
            is.close();

            JSONObject obj = new JSONObject(new String(buf, StandardCharsets.UTF_8));
            UserProfile profile = new UserProfile();
            profile.setFullName(obj.optString("fullName", ""));
            profile.setProfileImageUrl(obj.optString("profileImageUrl", ""));
            profile.setMobileNumber(obj.optString("mobileNumber", ""));
            profile.setEmail(obj.optString("email", ""));
            profile.setWhatsapp(obj.optString("whatsapp", ""));
            profile.setFacebook(obj.optString("facebook", ""));
            profile.setDateOfBirth(obj.optString("dateOfBirth", ""));
            profile.setSex(obj.optString("sex", ""));
            profile.setUserId(obj.optString("userId", ""));
            profile.setUserType(obj.optString("userType", ""));
            profile.setVerified(obj.optBoolean("isVerified", false));
            profile.setProvince(obj.optString("province", ""));
            profile.setDistrict(obj.optString("district", ""));
            profile.setMunicipality(obj.optString("municipality", ""));
            profile.setWardNo(obj.optString("wardNo", ""));
            profile.setTole(obj.optString("tole", ""));
            profile.setAbout(obj.optString("about", ""));
            return profile;

        } catch (Exception e) {
            Log.e(TAG, "loadProfile error: " + e.getMessage());
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // ── Load review summary from assets/reviews.json ──────────────────────────────
    private void loadReviewSummary() {
        try {
            InputStream is = getAssets().open("reviews.json");
            byte[] buf = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buf);
            is.close();

            JSONArray arr  = new JSONArray(new String(buf, StandardCharsets.UTF_8));
            int total      = Math.min(arr.length(), 15);
            float sum      = 0f;

            for (int i = 0; i < total; i++) {
                sum += (float) arr.getJSONObject(i).optDouble("rating", 0);
            }

            if (total > 0) {
                tvReviewStars.setText(String.format("⭐ %.1f", sum / total));
                tvReviewCount.setText("(" + total + " review" + (total > 1 ? "s" : "") + ")");
            } else {
                tvReviewStars.setText("⭐ 0.0");
                tvReviewCount.setText("(No reviews yet)");
            }

        } catch (Exception e) {
            tvReviewStars.setText("⭐ 0.0");
            tvReviewCount.setText("(No reviews yet)");
        }

        btnSeeReviews.setOnClickListener(v ->
                startActivity(new Intent(this, ReviewActivity.class)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    /** Returns true if the string is non-null and not blank */
    private boolean hasValue(String v) {
        return v != null && !v.trim().isEmpty();
    }

    /** Returns true if the given app package is installed */
    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** Opens a URL in the default browser */
    private void openInBrowser(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show();
        }
    }
}