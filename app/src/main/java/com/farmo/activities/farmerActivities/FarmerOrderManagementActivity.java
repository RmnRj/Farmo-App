package com.farmo.activities.farmerActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farmo.R;
import com.farmo.adapter.OrderAdapter;
import com.farmo.model.Order;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FarmerOrderManagementActivity extends AppCompatActivity {

    // ─── Constants ───────────────────────────────────────────────────────────
    private static final int PAGE_SIZE = 20;

    // ─── UI ──────────────────────────────────────────────────────────────────
    private RecyclerView rvOrders;
    private Button btnLoadMore;
    private TextView tvOrderType;
    private Spinner spinnerSort;

    // ─── Data ─────────────────────────────────────────────────────────────────
    /** Full data loaded from JSON (all records, all statuses). */
    private List<Order> allOrders = new ArrayList<>();

    /** Filtered view according to selected spinner option. */
    private List<Order> filteredOrders = new ArrayList<>();

    /** Items currently shown in the RecyclerView. */
    private List<Order> displayedOrders = new ArrayList<>();

    private OrderAdapter adapter;

    /** How many items from filteredOrders have been shown so far. */
    private int currentOffset = 0;

    /** Currently selected filter ("All", "Pending", "Rejected", "Accepted"). */
    private String currentFilter = "All";

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        initViews();
        setupSpinner();
        setupRecyclerView();

        // Step 1 – load raw data from JSON
        loadOrdersFromJson();

        // Step 2 – apply default filter ("All") and display first page
        applyFilterAndReset();
    }

    // ─── View wiring ──────────────────────────────────────────────────────────

    private void initViews() {
        rvOrders    = findViewById(R.id.rvProducts);
        btnLoadMore = findViewById(R.id.btnLoadMore);
        tvOrderType = findViewById(R.id.tvOrderType);
        spinnerSort = findViewById(R.id.spinnerSort);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnLoadMore.setOnClickListener(v -> loadNextPage());
    }

    private void setupSpinner() {
        String[] filterOptions = {"All", "Pending", "Accepted", "Rejected"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filterOptions[position];
                applyFilterAndReset();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(this, displayedOrders);
        adapter.setOnOrderActionListener(new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onAccept(Order order, int position) {
                handleAcceptOrder(order, position);
            }

            @Override
            public void onReject(Order order, int position) {
                handleRejectOrder(order, position);
            }
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
        rvOrders.setNestedScrollingEnabled(false);
    }

    // ─── Order Actions ────────────────────────────────────────────────────────

    private void handleAcceptOrder(Order order, int position) {
        order.setStatus("Accepted");
        adapter.notifyItemChanged(position);

        // Update in allOrders and filteredOrders as well
        updateOrderStatus(order.getId(), "Accepted");

        Toast.makeText(this, "Order " + order.getId() + " accepted", Toast.LENGTH_SHORT).show();

        // If filter is set to "Pending", remove this item after a delay
        if (currentFilter.equals("Pending")) {
            rvOrders.postDelayed(() -> {
                displayedOrders.remove(position);
                filteredOrders.remove(order);
                updateUI();
            }, 500);
        }
    }

    private void handleRejectOrder(Order order, int position) {
        order.setStatus("Rejected");
        adapter.notifyItemChanged(position);

        // Update in allOrders and filteredOrders as well
        updateOrderStatus(order.getId(), "Rejected");

        Toast.makeText(this, "Order " + order.getId() + " rejected", Toast.LENGTH_SHORT).show();

        // If filter is set to "Pending", remove this item after a delay
        if (currentFilter.equals("Pending")) {
            rvOrders.postDelayed(() -> {
                displayedOrders.remove(position);
                filteredOrders.remove(order);
                updateUI();
            }, 500);
        }
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        for (Order order : allOrders) {
            if (order.getId().equals(orderId)) {
                order.setStatus(newStatus);
                break;
            }
        }
    }

    // ─── DATA LAYER ───────────────────────────────────────────────────────────

    /**
     * Loads ALL orders from the local assets/orders.json file.
     */
    private void loadOrdersFromJson() {
        allOrders.clear();
        try {
            // Read file from assets/orders.json
            InputStream is = getAssets().open("orders.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Order order = new Order(
                        obj.getString("id"),
                        obj.getString("customerName"),
                        obj.getString("product"),
                        obj.getInt("quantity"),
                        obj.getDouble("totalPrice"),
                        obj.getString("status"),
                        obj.getString("date"),
                        obj.optString("userId", "USER" + (1000 + i)),
                        obj.optString("shippingAddress", "123 Main St, City, State"),
                        obj.optString("expectedDeliveryDate", "2025-03-15")
                );
                allOrders.add(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ─── FILTER + PAGINATION ──────────────────────────────────────────────────

    /**
     * Applies the current filter to allOrders, resets pagination,
     * and loads the first page.
     */
    private void applyFilterAndReset() {
        filteredOrders.clear();
        currentOffset = 0;

        for (Order order : allOrders) {
            if (currentFilter.equals("All") || order.getStatus().equals(currentFilter)) {
                filteredOrders.add(order);
            }
        }

        // Update the section title label
        String label = currentFilter.equals("All") ? "All Orders" : currentFilter + " Orders";
        updateOrderTypeLabel(label);

        // Clear the displayed list and show first page
        displayedOrders.clear();
        adapter.notifyDataSetChanged();

        loadNextPage();
    }

    /**
     * Appends the next PAGE_SIZE items from filteredOrders into displayedOrders.
     */
    private void loadNextPage() {
        int start = currentOffset;
        int end   = Math.min(start + PAGE_SIZE, filteredOrders.size());

        if (start >= filteredOrders.size()) {
            Toast.makeText(this, "No more orders to load.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Order> nextPage = filteredOrders.subList(start, end);
        displayedOrders.addAll(nextPage);
        currentOffset = end;

        updateUI();
    }

    // ─── UI LAYER ─────────────────────────────────────────────────────────────

    /**
     * Refreshes the RecyclerView and toggles the Load More button visibility.
     */
    private void updateUI() {
        adapter.notifyDataSetChanged();

        boolean hasMore = currentOffset < filteredOrders.size();
        btnLoadMore.setVisibility(hasMore ? View.VISIBLE : View.GONE);

        if (displayedOrders.isEmpty()) {
            tvOrderType.setText(currentFilter.equals("All") ? "All Orders (0)" :
                    currentFilter + " Orders (0)");
        } else {
            String label = currentFilter.equals("All") ? "All Orders" : currentFilter + " Orders";
            tvOrderType.setText(label + " (" + filteredOrders.size() + ")");
        }
    }

    /**
     * Updates only the order-type label TextView.
     */
    private void updateOrderTypeLabel(String label) {
        tvOrderType.setText(label);
    }
}