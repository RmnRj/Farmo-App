package com.farmo.activities.commonActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

    private static final int PAGE_SIZE = 14;

    // Views
    private RecyclerView rvProducts;
    private Button btnLoadMore;
    private ImageView btnBack;
    private TextView tvCategoryTitle, tvItemCount;
    private Spinner spinnerSort, spinnerFarmer;

    // Data Lists
    private OrderAdapter.BazarProductAdapter adapter;
    private List<BazarProduct> allProducts = new ArrayList<>();       // Original source
    private List<BazarProduct> filteredProducts = new ArrayList<>();  // Filtered/Sorted subset
    private List<BazarProduct> displayedProducts = new ArrayList<>(); // Pagination subset

    private int currentPage = 0;
    private boolean hasNextPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list); // Using your layout name

        initViews();
        setupSpinners();
        loadProductsFromJson();
        applyFiltersAndSort(); // Initial render
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCategoryTitle = findViewById(R.id.tvCategoryName);
        tvItemCount = findViewById(R.id.tvItemCount);
        btnLoadMore = findViewById(R.id.btnShowMore);
        rvProducts = findViewById(R.id.recyclerView);
        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerFarmer = findViewById(R.id.spinnerFarmer);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new OrderAdapter.BazarProductAdapter(this, displayedProducts);
        rvProducts.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnLoadMore.setOnClickListener(v -> {
            if (hasNextPage) loadPage(currentPage + 1);
        });
    }

    private void setupSpinners() {
        // Sort Spinner Setup
        String[] sortOptions = {"Best Match", "Price: Low to High"};
        ArrayAdapter<String> sAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sAdapter);

        // Farmer Spinner Setup
        String[] farmerOptions = {"All Farmers", "Connected Farmers Only"};
        ArrayAdapter<String> fAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmerOptions);
        fAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFarmer.setAdapter(fAdapter);

        // Selection Listener
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFiltersAndSort();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerSort.setOnItemSelectedListener(listener);
        spinnerFarmer.setOnItemSelectedListener(listener);
    }

    private void applyFiltersAndSort() {
        String farmerCriteria = spinnerFarmer.getSelectedItem().toString();
        int sortType = spinnerSort.getSelectedItemPosition();

        // 1. Filter Logic
        filteredProducts.clear();
        for (BazarProduct p : allProducts) {
            if (farmerCriteria.equals("All Farmers") || p.isConnected()) {
                filteredProducts.add(p);
            }
        }

        // 2. Sort Logic
        if (sortType == 0) { // Best Match -> Sort by Rating (High to Low)
            Collections.sort(filteredProducts, (p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));
        } else if (sortType == 1) { // Price -> Low to High
            Collections.sort(filteredProducts, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        }

        // 3. Update Item Count Badge
        tvItemCount.setText(filteredProducts.size() + " items found");

        // 4. Reset Pagination and Display
        displayedProducts.clear();
        loadPage(0);
    }

    private void loadPage(int page) {
        currentPage = page;
        int fromIndex = page * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredProducts.size());

        if (fromIndex < filteredProducts.size()) {
            List<BazarProduct> pageItems = filteredProducts.subList(fromIndex, toIndex);
            displayedProducts.addAll(pageItems);
            hasNextPage = toIndex < filteredProducts.size();
        } else {
            hasNextPage = false;
        }

        adapter.notifyDataSetChanged();
        btnLoadMore.setVisibility(hasNextPage ? View.VISIBLE : View.GONE);
    }

    private void loadProductsFromJson() {
        try {
            String json = readAssetFile("bazarproduct.json");
            JSONObject root = new JSONObject(json);
            JSONArray jsonArray = root.has("products") ? root.getJSONArray("products") : new JSONArray(json);

            allProducts.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                BazarProduct p = new BazarProduct();
                p.setId(obj.optInt("id", 0));
                p.setName(obj.optString("name", ""));
                p.setPrice(obj.optDouble("price", 0.0));
                p.setDiscount(obj.optInt("discount", 0));
                p.setRating(obj.optDouble("rating", 0.0));
                p.setReviewCount(obj.optInt("review_count", 0));
                p.setImageUrl(obj.optString("image_url", ""));
                p.setConnected(obj.optBoolean("is_connected", false));
                allProducts.add(p);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
        }
    }

    private String readAssetFile(String fileName) throws IOException {
        InputStream is = getAssets().open(fileName);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }
}