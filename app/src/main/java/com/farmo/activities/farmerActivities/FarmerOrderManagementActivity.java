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

    // ─── Constants ────────────────────────────────────────────────────────────
    private static final int PAGE_SIZE = 10;

    // ─── UI ───────────────────────────────────────────────────────────────────
    private RecyclerView rvOrders;
    private Button       btnLoadMore;
    private TextView     tvOrderType;
    private Spinner      spinnerSort;

    // ─── Data ─────────────────────────────────────────────────────────────────
    /** Full list loaded from JSON — never modified after load. */
    private final List<Order> allOrders      = new ArrayList<>();

    /** Subset of allOrders matching the current filter. */
    private final List<Order> filteredOrders = new ArrayList<>();

    /** Items currently shown in the RecyclerView. */
    private final List<Order> displayedOrders = new ArrayList<>();

    private OrderAdapter adapter;
    private int          currentOffset = 0;
    private String       currentFilter = "All";

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        initViews();
        setupSpinner();
        setupRecyclerView();

        // 1. Load raw data from JSON into allOrders
        loadOrdersFromJson();

        // 2. Apply default "All" filter and render first page
        applyFilterAndReset();
    }

    // =========================================================================
    //  VIEW WIRING
    // =========================================================================

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

    // =========================================================================
    //  ORDER ACTIONS
    // =========================================================================

    private void handleAcceptOrder(Order order, int position) {
        order.setStatus("Accepted");
        syncStatusToAllOrders(order.getId(), "Accepted");
        adapter.notifyItemChanged(position);
        Toast.makeText(this, "Order " + order.getId() + " accepted", Toast.LENGTH_SHORT).show();

        if (currentFilter.equals("Pending")) {
            removeOrderAfterDelay(order, position);
        }
    }

    private void handleRejectOrder(Order order, int position) {
        order.setStatus("Rejected");
        syncStatusToAllOrders(order.getId(), "Rejected");
        adapter.notifyItemChanged(position);
        Toast.makeText(this, "Order " + order.getId() + " rejected", Toast.LENGTH_SHORT).show();

        if (currentFilter.equals("Pending")) {
            removeOrderAfterDelay(order, position);
        }
    }

    /** Removes an order from the displayed + filtered lists after a short delay. */
    private void removeOrderAfterDelay(Order order, int position) {
        rvOrders.postDelayed(() -> {
            if (position < displayedOrders.size()) {
                displayedOrders.remove(position);
                adapter.notifyItemRemoved(position);
            }
            filteredOrders.remove(order);
            refreshUI();
        }, 500);
    }

    /** Keeps allOrders in sync when a status changes. */
    private void syncStatusToAllOrders(String orderId, String newStatus) {
        for (Order o : allOrders) {
            if (o.getId().equals(orderId)) {
                o.setStatus(newStatus);
                break;
            }
        }
    }

    // =========================================================================
    //  DATA LAYER  — loads from JSON, no UI changes here
    // =========================================================================

    /**
     * Reads assets/orders.json and populates {@code allOrders}.
     * Pure data operation — does NOT touch any view.
     */
    private void loadOrdersFromJson() {
        allOrders.clear();
        try {
            InputStream is     = getAssets().open("orders.json");
            byte[]      buffer = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();

            JSONArray jsonArray = new JSONArray(new String(buffer, StandardCharsets.UTF_8));

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                allOrders.add(new Order(
                        obj.getString("id"),
                        obj.getString("customerName"),
                        obj.getString("product"),
                        obj.getInt("quantity"),
                        obj.getDouble("totalPrice"),
                        obj.getString("status"),
                        obj.getString("date"),
                        obj.optString("userId",              "USER" + (1000 + i)),
                        obj.optString("shippingAddress",     "N/A"),
                        obj.optString("expectedDeliveryDate","N/A")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load orders: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // =========================================================================
    //  FILTER + PAGINATION
    // =========================================================================

    /**
     * Rebuilds {@code filteredOrders} from {@code allOrders} based on
     * {@code currentFilter}, resets pagination, then loads the first page.
     */
    private void applyFilterAndReset() {
        filteredOrders.clear();
        currentOffset = 0;

        for (Order order : allOrders) {
            if (currentFilter.equals("All") || order.getStatus().equals(currentFilter)) {
                filteredOrders.add(order);
            }
        }

        displayedOrders.clear();
        adapter.notifyDataSetChanged();

        loadNextPage();
    }

    /**
     * Appends the next PAGE_SIZE items from {@code filteredOrders}
     * into {@code displayedOrders} and refreshes the UI.
     */
    private void loadNextPage() {
        int start = currentOffset;
        int end   = Math.min(start + PAGE_SIZE, filteredOrders.size());

        if (start >= filteredOrders.size()) {
            Toast.makeText(this, "No more orders.", Toast.LENGTH_SHORT).show();
            btnLoadMore.setVisibility(View.GONE);
            return;
        }

        displayedOrders.addAll(filteredOrders.subList(start, end));
        currentOffset = end;

        refreshUI();
    }

    // =========================================================================
    //  UI LAYER  — updates views only, no data loading here
    // =========================================================================

    /**
     * Refreshes the RecyclerView, the header label, and the
     * "See More" button visibility.
     * Pure UI operation — does NOT touch allOrders or filteredOrders.
     */
    private void refreshUI() {
        adapter.notifyDataSetChanged();

        // "See More" button — visible only when more pages exist
        boolean hasMore = currentOffset < filteredOrders.size();
        btnLoadMore.setVisibility(hasMore ? View.VISIBLE : View.GONE);

        // Section label
        String base  = currentFilter.equals("All") ? "All Orders" : currentFilter + " Orders";
        String label = filteredOrders.isEmpty()
                ? base + " (0)"
                : base + " (" + displayedOrders.size() + " of " + filteredOrders.size() + ")";
        tvOrderType.setText(label);
    }
}