package com.farmo.activities.commonActivities;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager2.widget.ViewPager2;

import com.farmo.adapter.ImageSliderAdapter;
import com.farmo.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetail";

    // ── Main screen views ─────────────────────────────────────────────────────
    private ProgressBar      progressBar;
    private NestedScrollView layoutContent;
    private TextView         tvProductName, tvPrice, tvOldPrice, tvUnit, tvDescription;
    private TextView         tvSoldCount, tvAvailableQty, tvRatingNum, tvRatingCount, tvImageCounter;
    private TextView         tvCategory, tvMinOrder, tvType, tvHarvestDate, tvOrigin;
    private TextView         tvFarmerName, tvFarmerLocation;
    private TextView         tvReadMore, btnSeeReviews;
    private ImageView        btnBack, btnShare, btnWishlist;
    private ViewPager2       viewPagerImages;
    private Button           btnReviewIt, btnReqestOrder;
    private RatingBar        ratingBar;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean isDescriptionExpanded = false;
    private int     orderQty              = 1;
    private double  loadedFinalPrice      = 0.0;
    private String  loadedUnit            = "unit";

    // ── Review pagination ─────────────────────────────────────────────────────
    private static final int PAGE_SIZE     = 4;
    private List<JSONObject>  allReviews   = new ArrayList<>();
    private int               reviewsShown = 0;

    // ── Review dialog views ───────────────────────────────────────────────────
    private LinearLayout reviewsContainer;
    private TextView     tvNoReviews;
    private Button       btnLoadMoreReviews;
    private AlertDialog  reviewsDialog;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        initViews();
        setupClickListeners();
        loadProductData();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 1. INIT VIEWS
    // ══════════════════════════════════════════════════════════════════════════

    private void initViews() {
        progressBar      = findViewById(R.id.progressBar);
        layoutContent    = findViewById(R.id.layoutContent);
        tvProductName    = findViewById(R.id.tvProductName);
        tvPrice          = findViewById(R.id.tvPrice);
        tvOldPrice       = findViewById(R.id.tvOldPrice);
        tvUnit           = findViewById(R.id.tvUnit);
        tvDescription    = findViewById(R.id.tvDescription);
        tvSoldCount      = findViewById(R.id.tvSoldCount);
        tvAvailableQty   = findViewById(R.id.tvAvailableQty);
        tvRatingNum      = findViewById(R.id.tvRatingNum);
        tvRatingCount    = findViewById(R.id.tvRatingCount);
        tvCategory       = findViewById(R.id.tvCategory);
        tvMinOrder       = findViewById(R.id.tvMinOrder);
        tvType           = findViewById(R.id.tvType);
        tvHarvestDate    = findViewById(R.id.tvHarvestDate);
        tvOrigin         = findViewById(R.id.tvOrigin);
        tvFarmerName     = findViewById(R.id.tvFarmerName);
        tvFarmerLocation = findViewById(R.id.tvFarmerLocation);
        viewPagerImages  = findViewById(R.id.viewPagerImages);
        btnBack          = findViewById(R.id.btnBack);
        btnShare         = findViewById(R.id.btnShare);
        btnWishlist      = findViewById(R.id.btnWishlist);
        btnReviewIt      = findViewById(R.id.btnReviewIt);
        btnReqestOrder   = findViewById(R.id.btnReqestOrder);
        btnSeeReviews    = findViewById(R.id.btnSeeReviews);
        ratingBar        = findViewById(R.id.ratingBar);
        tvReadMore       = findViewById(R.id.tvReadMore);
        tvImageCounter   = findViewById(R.id.tvImageCounter);

        tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 2. CLICK LISTENERS
    // ══════════════════════════════════════════════════════════════════════════

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Share clicked", Toast.LENGTH_SHORT).show());

        btnWishlist.setOnClickListener(v ->
                Toast.makeText(this, "Added to wishlist!", Toast.LENGTH_SHORT).show());

        tvReadMore.setOnClickListener(v -> {
            if (!isDescriptionExpanded) {
                tvDescription.setMaxLines(Integer.MAX_VALUE);
                tvDescription.setEllipsize(null);
                tvReadMore.setText("Read less ▲");
                isDescriptionExpanded = true;
            } else {
                tvDescription.setMaxLines(4);
                tvDescription.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvReadMore.setText("Read more ▼");
                isDescriptionExpanded = false;
            }
        });

        btnSeeReviews.setOnClickListener(v -> showReviewsDialog());
        btnReviewIt.setOnClickListener(v -> showRateDialog());
        btnReqestOrder.setOnClickListener(v -> showOrderPopup());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 3. ORDER POPUP  — matches See Reviews logic using AlertDialog
    // ══════════════════════════════════════════════════════════════════════════

    private void showOrderPopup() {
        // Inflate popup layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_order_request, null);

        // ── Create AlertDialog ────────────────────────────────────────────────
        AlertDialog orderDialog = new AlertDialog.Builder(this, R.style.RoundedDialog)
                .setView(popupView)
                .create();

        // ── Bind views directly ───────────────────────────────────────────────
        ImageButton btnClose        = popupView.findViewById(R.id.btnClosePopup);
        View        btnIncrease     = popupView.findViewById(R.id.btnIncrease);
        View        btnDecrease     = popupView.findViewById(R.id.btnDecrease);
        View        btnOrderRequest = popupView.findViewById(R.id.btnOrderRequest);

        TextView tvQuantity  = popupView.findViewById(R.id.tvQuantity);
        TextView tvTotalCost = popupView.findViewById(R.id.tvTotalCost);
        EditText etExpected  = popupView.findViewById(R.id.tvExpectedDelivery);

        EditText etProvince  = popupView.findViewById(R.id.etProvince);
        EditText etDistrict  = popupView.findViewById(R.id.etDistrict);
        EditText etMunicipal = popupView.findViewById(R.id.etMunicipal);
        EditText etWard      = popupView.findViewById(R.id.etWard);
        EditText etTole      = popupView.findViewById(R.id.etTole);

        // ── Reset quantity each time popup opens ──────────────────────────────
        orderQty = 1;
        tvQuantity.setText(String.valueOf(orderQty));
        tvTotalCost.setText(String.format("Rs. %.0f", loadedFinalPrice * orderQty));

        // ── + button ──────────────────────────────────────────────────────────
        btnIncrease.setOnClickListener(v -> {
            orderQty++;
            tvQuantity.setText(String.valueOf(orderQty));
            tvTotalCost.setText(String.format("Rs. %.0f", loadedFinalPrice * orderQty));
        });

        // ── − button ──────────────────────────────────────────────────────────
        btnDecrease.setOnClickListener(v -> {
            if (orderQty > 1) {
                orderQty--;
                tvQuantity.setText(String.valueOf(orderQty));
                tvTotalCost.setText(String.format("Rs. %.0f", loadedFinalPrice * orderQty));
            }
        });

        // ── Close ─────────────────────────────────────────────────────────────
        btnClose.setOnClickListener(v -> orderDialog.dismiss());

        // ── Submit ────────────────────────────────────────────────────────────
        btnOrderRequest.setOnClickListener(v -> {
            String province  = safeText(etProvince);
            String district  = safeText(etDistrict);
            String municipal = safeText(etMunicipal);
            String ward      = safeText(etWard);
            String tole      = safeText(etTole);
            String delivery  = etExpected != null && etExpected.getText() != null
                    ? etExpected.getText().toString().trim() : "";

            if (province.isEmpty() || district.isEmpty()
                    || municipal.isEmpty() || ward.isEmpty() || tole.isEmpty()) {
                Toast.makeText(this,
                        "Please fill in all delivery address fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String address     = tole + ", Ward-" + ward + ", " + municipal
                    + ", " + district + ", " + province;
            String deliveryStr = delivery.isEmpty()
                    ? "Not specified" : "Within " + delivery + " day(s)";

            // Confirm with a simple AlertDialog
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Order")
                    .setMessage(
                            "🔢  Qty      : " + orderQty + " " + loadedUnit + "\n" +
                                    "💰  Total    : Rs. " + String.format("%.0f", loadedFinalPrice * orderQty) + "\n" +
                                    "🚚  Delivery : " + deliveryStr + "\n" +
                                    "📍  Address  :\n    " + address + "\n\n" +
                                    "Confirm this order?"
                    )
                    .setPositiveButton("✅ Confirm", (d, w) -> {
                        Toast.makeText(this,
                                "Order placed successfully! 🎉",
                                Toast.LENGTH_LONG).show();
                        orderDialog.dismiss();
                    })
                    .setNegativeButton("✏️ Edit", null)
                    .show();
        });

        // ── Show Dialog ──────────────────
        orderDialog.show();
    }

    // ── Safe getText helper for EditText ──────────────────────────────────────
    private String safeText(EditText field) {
        if (field == null) return "";
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 4. LOAD PRODUCT DATA
    // ══════════════════════════════════════════════════════════════════════════

    private void loadProductData() {
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(() -> {
            try {
                String json = loadJSONFromAsset("product.json");
                if (json != null) {
                    JSONArray  array = new JSONArray(json);
                    JSONObject p     = array.getJSONObject(0);
                    updateUI(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load product data", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
        }, 900);
    }

    private String loadJSONFromAsset(String fileName) {
        try {
            InputStream is  = getAssets().open(fileName);
            byte[]      buf = new byte[is.available()];
            is.read(buf);
            is.close();
            return new String(buf, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 5. UPDATE UI FROM JSON
    // ══════════════════════════════════════════════════════════════════════════

    private void updateUI(JSONObject p) throws Exception {

        tvProductName.setText(p.getString("name"));
        tvCategory.setText(p.getString("category"));
        tvDescription.setText(p.getString("description"));
        tvFarmerName.setText(p.getString("farmer_name"));

        // Price + discount
        double cost         = Double.parseDouble(p.getString("Cost_per_unit"));
        double discountVal  = Double.parseDouble(p.optString("discount_value", "0"));
        String discountType = p.optString("discount_type", "Percentage");

        double finalPrice = discountType.equalsIgnoreCase("Fixed")
                ? cost - discountVal
                : cost - (cost * discountVal / 100.0);
        finalPrice = Math.max(finalPrice, 0);

        loadedFinalPrice = finalPrice;
        loadedUnit       = p.optString("Unit", "unit");

        tvPrice.setText(String.format("Rs. %.0f", finalPrice));
        tvOldPrice.setText(String.format("Rs. %.0f", cost));
        tvUnit.setText("/ " + loadedUnit);

        // Discount badge
        TextView tvDiscountBadge = findViewById(R.id.tvDiscountBadge);
        if (discountVal > 0) {
            tvDiscountBadge.setVisibility(View.VISIBLE);
            tvDiscountBadge.setText(discountType.equalsIgnoreCase("Fixed")
                    ? "- Rs." + (int) discountVal
                    : "-" + (int) discountVal + "%");
        } else {
            tvDiscountBadge.setVisibility(View.GONE);
        }

        // Organic badge
        boolean isOrganic = p.optBoolean("is_organic", false);
        findViewById(R.id.tvOrganicBadge).setVisibility(isOrganic ? View.VISIBLE : View.GONE);
        tvType.setText(isOrganic ? "Organic" : "Conventional");

        // Stock
        boolean inStock = p.optBoolean("in_Stock", true);
        if (tvAvailableQty != null)
            tvAvailableQty.setText(inStock ? "In Stock" : "Out of Stock");

        tvHarvestDate.setText(p.optString("produced_date", "N/A"));
        tvOrigin.setText(p.optString("farmer_location", "Nepal"));
        tvFarmerLocation.setText("📍 " + p.optString("farmer_location", "Nepal"));

        // Farmer verified
        TextView tvFarmerVerified = findViewById(R.id.tvFarmerVerified);
        if (tvFarmerVerified != null)
            tvFarmerVerified.setVisibility(
                    p.optBoolean("farmer_verified", false) ? View.VISIBLE : View.GONE);

        // Farmer initial avatar
        TextView tvFarmerInitial = findViewById(R.id.tvFarmerInitial);
        String farmerName = p.optString("farmer_name", "?");
        if (tvFarmerInitial != null)
            tvFarmerInitial.setText(String.valueOf(farmerName.charAt(0)).toUpperCase());

        if (tvMinOrder != null) tvMinOrder.setText("0.5 " + loadedUnit);

        // Rating
        float rating      = (float) p.optDouble("rating", 0);
        int   ratingCount = p.optInt("rating_count", 0);
        ratingBar.setRating(rating);
        tvRatingNum.setText(String.valueOf(rating));
        tvRatingCount.setText("(" + ratingCount + " reviews)");

        // sold_count is "2k" string in JSON
        tvSoldCount.setText(p.optString("sold_count", "0") + " sold");

        // Image slider
        JSONArray    media  = p.optJSONArray("media");
        List<String> images = new ArrayList<>();
        if (media != null)
            for (int i = 0; i < media.length(); i++) images.add(media.optString(i));
        if (images.isEmpty()) images.add("");

        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPagerImages.setAdapter(adapter);
        tvImageCounter.setText("1 / " + images.size());
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                tvImageCounter.setText((position + 1) + " / " + images.size());
            }
        });

        // Product status
        TextView tvProductStatus = findViewById(R.id.tvProductStatus);
        if (tvProductStatus != null) {
            tvProductStatus.setText(inStock ? "● In Stock" : "● Out of Stock");
            tvProductStatus.setTextColor(inStock
                    ? getColor(R.color.green_primary) : 0xFFCC0000);
        }

        // Read more
        tvDescription.post(() -> {
            if (tvDescription.getLayout() != null) {
                int     lines    = tvDescription.getLayout().getLineCount();
                boolean overflow = lines > 4
                        || tvDescription.getLayout().getEllipsisCount(lines - 1) > 0;
                tvReadMore.setVisibility(overflow ? View.VISIBLE : View.GONE);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 6. REVIEWS DIALOG
    // ══════════════════════════════════════════════════════════════════════════

    private void showReviewsDialog() {
        allReviews.clear();
        reviewsShown = 0;

        try {
            String json = loadJSONFromAsset("reviews.json");
            if (json != null) {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) allReviews.add(arr.getJSONObject(i));
            }
        } catch (Exception e) { e.printStackTrace(); }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reviews, null);
        reviewsContainer   = dialogView.findViewById(R.id.reviewsContainer);
        tvNoReviews        = dialogView.findViewById(R.id.tvNoReviews);
        btnLoadMoreReviews = dialogView.findViewById(R.id.btnLoadMoreReviews);

        reviewsDialog = new AlertDialog.Builder(this, R.style.RoundedDialog)
                .setView(dialogView)
                .create();

        TextView tvReviewsTitle = dialogView.findViewById(R.id.tvReviewsTitle);
        tvReviewsTitle.setText("Reviews (" + allReviews.size() + ")");

        if (allReviews.isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
            btnLoadMoreReviews.setVisibility(View.GONE);
        } else {
            tvNoReviews.setVisibility(View.GONE);
            loadMoreReviews();
        }

        btnLoadMoreReviews.setOnClickListener(v -> loadMoreReviews());
        reviewsDialog.show();
    }

    private void loadMoreReviews() {
        int end = Math.min(reviewsShown + PAGE_SIZE, allReviews.size());
        for (int i = reviewsShown; i < end; i++) {
            try { reviewsContainer.addView(buildReviewCard(allReviews.get(i))); }
            catch (Exception e) { e.printStackTrace(); }
        }
        reviewsShown = end;
        if (reviewsShown < allReviews.size()) {
            btnLoadMoreReviews.setVisibility(View.VISIBLE);
            btnLoadMoreReviews.setText("Load More Reviews ("
                    + (allReviews.size() - reviewsShown) + " remaining)");
        } else {
            btnLoadMoreReviews.setVisibility(View.GONE);
        }
    }

    private View buildReviewCard(JSONObject r) throws Exception {
        String name    = r.optString("reviewer_name", "Anonymous");
        float  stars   = (float) r.optDouble("rating", 0);
        String comment = r.optString("comment", "");
        String date    = r.optString("date", "");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, dpToPx(16));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(this);
        int sz = dpToPx(40);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(sz, sz);
        avLp.setMarginEnd(dpToPx(12));
        avatar.setLayoutParams(avLp);
        avatar.setGravity(Gravity.CENTER);
        avatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        avatar.setTextSize(16);
        avatar.setTextColor(0xFFFFFFFF);
        avatar.setTypeface(null, android.graphics.Typeface.BOLD);
        avatar.setBackground(makeCircleDrawable(0xFF2D6A4F));
        row.addView(avatar);

        LinearLayout nameCol = new LinearLayout(this);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextSize(14);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setTextColor(0xFF111D15);
        nameCol.addView(tvName);

        TextView tvDate = new TextView(this);
        tvDate.setText(date);
        tvDate.setTextSize(11);
        tvDate.setTextColor(0xFF9EADA4);
        nameCol.addView(tvDate);
        row.addView(nameCol);

        RatingBar rb = new RatingBar(this, null, android.R.attr.ratingBarStyleSmall);
        rb.setNumStars(5);
        rb.setStepSize(1f);
        rb.setRating(stars);
        rb.setIsIndicator(true);
        rb.getProgressDrawable().setColorFilter(0xFFF4A100,
                android.graphics.PorterDuff.Mode.SRC_ATOP);
        row.addView(rb);
        card.addView(row);

        if (!comment.isEmpty()) {
            TextView tvComment = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dpToPx(8);
            tvComment.setLayoutParams(lp);
            tvComment.setText(comment);
            tvComment.setTextSize(13);
            tvComment.setTextColor(0xFF5C6B62);
            tvComment.setLineSpacing(dpToPx(3), 1f);
            card.addView(tvComment);
        }

        View div = new View(this);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        dlp.topMargin = dpToPx(14);
        div.setLayoutParams(dlp);
        div.setBackgroundColor(0xFFEEF2EE);
        card.addView(div);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 7. RATE DIALOG
    // ══════════════════════════════════════════════════════════════════════════

    private void showRateDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate_product, null);

        RatingBar               ratingInput = dialogView.findViewById(R.id.dialogRatingBar);
        EditText etReview    = dialogView.findViewById(R.id.etReviewText);
        TextView                tvSubmit    = dialogView.findViewById(R.id.tvSubmitRating);
        TextView                tvCancel    = dialogView.findViewById(R.id.tvCancelRating);

        tvSubmit.setBackgroundResource(R.drawable.btn_green_gradient);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.RoundedDialog)
                .setView(dialogView)
                .create();

        tvSubmit.setOnClickListener(v -> {
            float stars = ratingInput.getRating();
            if (stars == 0) {
                Toast.makeText(this, "Please select a star rating", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Review submitted! " + (int) stars + "⭐",
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        tvCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private android.graphics.drawable.ShapeDrawable makeCircleDrawable(int color) {
        android.graphics.drawable.ShapeDrawable d =
                new android.graphics.drawable.ShapeDrawable(
                        new android.graphics.drawable.shapes.OvalShape());
        d.getPaint().setColor(color);
        return d;
    }
}
