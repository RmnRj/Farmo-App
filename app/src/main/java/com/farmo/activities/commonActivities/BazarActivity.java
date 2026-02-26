package com.farmo.activities.commonActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farmo.R;
import com.farmo.adapter.OrderAdapter;
import com.farmo.model.BazarProduct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BazarActivity extends AppCompatActivity {

    private static final String TAG       = "BazarActivity";
    private static final int    PAGE_SIZE = 10;

    // ── Views ─────────────────────────────────────────────────────────────
    private RecyclerView  recyclerView;
    private Button        btnShowMore;
    private android.widget.ImageView btnBack;
    private TextView      tvCategoryName;
    private Spinner       spinnerSort, spinnerFarmer;
    private ProgressBar   progressBar;

    // ── Data ──────────────────────────────────────────────────────────────
    private OrderAdapter.BazarProductAdapter adapter;

    private final List<BazarProduct> allProducts       = new ArrayList<>();
    private final List<BazarProduct> filteredProducts  = new ArrayList<>();
    private final List<BazarProduct> displayedProducts = new ArrayList<>();

    private int     currentPage   = 0;
    private boolean spinnersReady = false;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bazar);

        if (!initViews()) return;

        // 1. Load raw data — no UI changes inside
        loadProductsFromJson();

        // 2. Wire spinners — triggers applyFiltersAndSort on first selection
        setupSpinners();
        spinnersReady = true;

        // 3. Apply default filter and render first page
        applyFiltersAndSort();
    }

    // =========================================================================
    //  VIEW WIRING
    // =========================================================================

    private boolean initViews() {
        btnBack        = findViewById(R.id.btnBack);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        btnShowMore    = findViewById(R.id.btnShowMore);
        recyclerView   = findViewById(R.id.recyclerView);
        spinnerSort    = findViewById(R.id.spinnerSort);
        spinnerFarmer  = findViewById(R.id.spinnerFarmer);
        progressBar    = findViewById(R.id.progressBar);

        String[] names = {"btnBack","btnShowMore","recyclerView","spinnerSort","spinnerFarmer"};
        Object[] views = {btnBack, btnShowMore, recyclerView, spinnerSort, spinnerFarmer};
        for (int i = 0; i < views.length; i++) {
            if (views[i] == null) {
                showErrorAndFinish("View not found: R.id." + names[i]);
                return false;
            }
        }

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new OrderAdapter.BazarProductAdapter(this, displayedProducts);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnShowMore.setOnClickListener(v -> loadPage(currentPage + 1));

        return true;
    }

    private void setupSpinners() {
        String[] sortOptions   = {"Best Match", "Price: Low to High"};
        String[] farmerOptions = {"All Farmers", "Connected Farmers Only"};

        ArrayAdapter<String> sAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, sortOptions);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sAdapter);

        ArrayAdapter<String> fAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, farmerOptions);
        fAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFarmer.setAdapter(fAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (spinnersReady) applyFiltersAndSort();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerSort.setOnItemSelectedListener(listener);
        spinnerFarmer.setOnItemSelectedListener(listener);
    }

    // =========================================================================
    //  DATA LAYER — reads JSON, no UI changes inside
    // =========================================================================

    /**
     * Reads assets/bazarproduct.json and populates {@code allProducts}.
     * Pure data operation — does NOT touch any view.
     */
    private void loadProductsFromJson() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        try {
            InputStream is     = getAssets().open("bazarproduct.json");
            byte[]      buffer = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray;
            if (json.trim().startsWith("{")) {
                JSONObject root = new JSONObject(json);
                jsonArray = root.has("products")
                        ? root.getJSONArray("products") : new JSONArray();
            } else {
                jsonArray = new JSONArray(json);
            }

            allProducts.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                BazarProduct p = new BazarProduct();
                p.setId(obj.optInt("id", 0));
                p.setName(obj.optString("name", "Unknown"));
                p.setPrice(obj.optDouble("price", 0.0));
                p.setDiscount(obj.optInt("discount", 0));
                p.setRating(obj.optDouble("rating", 0.0));
                p.setReviewCount(obj.optInt("review_count", 0));
                p.setImageUrl(obj.optString("image_url", ""));
                p.setConnected(obj.optBoolean("is_connected", false));
                allProducts.add(p);
            }

            Log.d(TAG, "Loaded " + allProducts.size() + " products.");

        } catch (IOException e) {
            Log.e(TAG, "bazarproduct.json not found in assets/", e);
            Toast.makeText(this, "bazarproduct.json not found in assets/",
                    Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error: " + e.getMessage(), e);
            Toast.makeText(this, "JSON parse error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } finally {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
    }

    // =========================================================================
    //  FILTER + SORT + PAGINATION
    // =========================================================================

    /**
     * Rebuilds {@code filteredProducts} from {@code allProducts}, sorts it,
     * resets pagination, and loads the first page.
     */
    private void applyFiltersAndSort() {
        String farmerFilter = spinnerFarmer.getSelectedItem().toString();
        int    sortType     = spinnerSort.getSelectedItemPosition();

        filteredProducts.clear();
        for (BazarProduct p : allProducts) {
            if (farmerFilter.equals("All Farmers") || p.isConnected()) {
                filteredProducts.add(p);
            }
        }

        if (sortType == 0) {
            Collections.sort(filteredProducts,
                    (a, b) -> Double.compare(b.getRating(), a.getRating()));
        } else {
            Collections.sort(filteredProducts,
                    (a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        }

        displayedProducts.clear();
        adapter.notifyDataSetChanged();
        currentPage = 0;

        loadPage(0);
    }

    /**
     * Appends the next PAGE_SIZE items from {@code filteredProducts}
     * into {@code displayedProducts}, then refreshes the UI.
     */
    private void loadPage(int page) {
        currentPage = page;
        int from = page * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filteredProducts.size());

        if (from >= filteredProducts.size()) return;

        displayedProducts.addAll(filteredProducts.subList(from, to));
        refreshUI();
    }

    // =========================================================================
    //  UI LAYER — updates views only, no data loading here
    // =========================================================================

    /**
     * Refreshes the RecyclerView, header counts, shown-count label,
     * and "See More" button visibility.
     * Pure UI operation — does NOT modify any data list.
     */
    private void refreshUI() {
        adapter.notifyDataSetChanged();

        // "See More ▼" button
        boolean hasMore = (currentPage + 1) * PAGE_SIZE < filteredProducts.size();
        btnShowMore.setVisibility(hasMore ? View.VISIBLE : View.GONE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void showErrorAndFinish(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
        finish();
    }
}