package com.farmo.activities.farmerActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.widget.NestedScrollView;

import com.farmo.activities.LoginActivity;
import com.farmo.activities.OrdersActivity;
import com.farmo.activities.ProfileActivity;
import com.farmo.R;
import com.farmo.activities.ReviewsActivity;
import com.farmo.activities.wallet.WalletActivity;
import com.farmo.network.Dashboard.DashboardService;
import com.farmo.network.Dashboard.RefreshWallet;
import com.farmo.network.RetrofitClient;
import com.farmo.utils.SessionManager;
import com.google.gson.Gson;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "FarmerDashboard";

    private boolean isBalanceVisible = true;
    private String UserType;
    private String walletBalance = "0.00";
    private String fullName = "UserName";
    private String todaySales = "0.00";

    private SessionManager sessionManager;
    private RelativeLayout walletArea;
    private TextView tvSalesAmount, tvWalletBalance;
    private ImageView RefreshWalletbyImage;
    private EditText DashboardSearch;

    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView nestedScrollView;
    private boolean isManualRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        sessionManager = new SessionManager(this);
        UserType = sessionManager.getUserType();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.e(TAG, "User not logged in - Session Check Failed!");
            Log.d(TAG, "User Type: " + sessionManager.getUserType()); // Debugging

            //Toast.makeText(this, "Debug: Session Invalid", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            // Redirect to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setupUI();
        setupSwipeRefresh();
        fetchDashboardData();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        // 1. Check if swipeRefreshLayout exists before using it
        if (swipeRefreshLayout == null) {
            Log.e(TAG, "ERROR: swipe_refresh ID not found in XML");
            return; // Stop here to prevent crash
        }

        // 2. Find NestedScrollView from the Activity, not necessarily from swipeLayout
        nestedScrollView = findViewById(R.id.nested_scroll_view);

        swipeRefreshLayout.setColorSchemeResources(
                R.color.topical_forest,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark
        );

        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    swipeRefreshLayout.setEnabled(scrollY == 0);
                }
            });
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Manual refresh triggered");
            isManualRefresh = true;
            fetchDashboardData();
        });
    }

    private void setupUI() {
        ImageView ivVisibility = findViewById(R.id.ivVisibility);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        TextView btnProfile = findViewById(R.id.btnProfile);
        tvSalesAmount = findViewById(R.id.tvSalesAmount);
        walletArea = findViewById(R.id.Walletbox);
        RefreshWalletbyImage = findViewById(R.id.ivRefresh);
        DashboardSearch = findViewById(R.id.dashboard_search);

        // Visibility Toggle
        ivVisibility.setOnClickListener(v -> {
            if (isBalanceVisible) {
                tvWalletBalance.setText("*****");
                tvSalesAmount.setText("*****");
                ivVisibility.setImageResource(R.drawable.ic_visibility_off);
            } else {
                tvWalletBalance.setText(String.format("NRs. %s", walletBalance));
                tvSalesAmount.setText(String.format("NRs. %s", todaySales));
                ivVisibility.setImageResource(R.drawable.ic_visibility);
            }
            isBalanceVisible = !isBalanceVisible;
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardAddProduct).setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardOrders).setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardMyProducts).setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, MyProductsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardOrderAnalytics).setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardReviews).setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, ReviewsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardReviewsBottom).setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, ReviewsActivity.class);
            startActivity(intent);
        });

        walletArea.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, WalletActivity.class);
            startActivity(intent);
        });

        RefreshWalletbyImage.setOnClickListener(v -> {
            refreshWalletUI();
        });

        DashboardSearch.setOnClickListener(v -> {
            // Add search functionality
        });
    }

    private void fetchDashboardData() {
        Log.d(TAG, "Fetching dashboard data...");

        // Verify authentication before making request
        if (!sessionManager.isLoggedIn()) {
            Log.e(TAG, "User not authenticated");
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            isManualRefresh = false;
            return;
        }

        // Show loading indicator if manual refresh
        if (isManualRefresh && swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        // Make API call with authentication
        RetrofitClient.getApiService(this).getDashboard().enqueue(new Callback<DashboardService.DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardService.DashboardResponse> call,
                                   Response<DashboardService.DashboardResponse> response) {

                Log.d(TAG, "Dashboard API response code: " + response.code());

                // Stop refresh animation
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    DashboardService.DashboardResponse data = response.body();

                    Log.d(TAG, "Dashboard data received successfully");

                    // Save values safely
                    fullName = data.getUsername() != null ? data.getUsername() : "User";
                    walletBalance = data.getWallet_amt() != null ? data.getWallet_amt() : "0.00";
                    todaySales = data.getTodayIncome() != null ? data.getTodayIncome() : "0.00";

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        updateGreeting(fullName);

                        if (isBalanceVisible) {
                            tvWalletBalance.setText(String.format("NRs. %s", walletBalance));
                            tvSalesAmount.setText(String.format("NRs. %s", todaySales));
                        }
                    });

                    // Show success message ONLY on manual refresh
                    if (isManualRefresh) {
                        Toast.makeText(FarmerDashboardActivity.this,
                                "Dashboard refreshed", Toast.LENGTH_SHORT).show();
                        isManualRefresh = false;
                    }
                } else {
                    // Handle error response
                    String errorMessage = "Failed to load dashboard data";

                    if (response.code() == 401 || response.code() == 403) {
                        errorMessage = "Session expired. Please login again.";
                        sessionManager.clearSession();
                        Log.e(TAG, "Authentication failed: " + response.code());
                    } else if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response: " + errorBody);

                            DashboardService.DashboardResponse errorResponse =
                                    new Gson().fromJson(errorBody, DashboardService.DashboardResponse.class);

                            if (errorResponse != null && errorResponse.getError() != null) {
                                errorMessage = errorResponse.getError();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response", e);
                        }
                    }

                    Toast.makeText(FarmerDashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    isManualRefresh = false;
                }
            }

            @Override
            public void onFailure(Call<DashboardService.DashboardResponse> call, Throwable t) {
                Log.e(TAG, "Dashboard API call failed", t);

                // Stop refresh animation
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                Toast.makeText(FarmerDashboardActivity.this,
                        "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                isManualRefresh = false;
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void updateGreeting(String name) {
        int timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (timeOfDay < 12) {
            greeting = "Good Morning";
        } else if (timeOfDay < 16) {
            greeting = "Good Afternoon";
        } else if (timeOfDay < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        tvGreeting.setText(String.format("%s, %s", greeting, name));
    }

    private void refreshWalletUI() {
        Log.d(TAG, "Refreshing wallet data...");

        RetrofitClient.getApiService(this).getRefreshWallet()
                .enqueue(new Callback<RefreshWallet.refreshWalletResponse>() {
                    @Override
                    public void onResponse(Call<RefreshWallet.refreshWalletResponse> call,
                                           Response<RefreshWallet.refreshWalletResponse> response) {

                        Log.d(TAG, "Wallet refresh response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            RefreshWallet.refreshWalletResponse data = response.body();

                            String balance = data.getBalance() != null ? data.getBalance() : "0.00";
                            String todaysIncome = data.getTodaysIncome() != null ? data.getTodaysIncome() : "0.00";

                            // Update local variables
                            walletBalance = balance;
                            todaySales = todaysIncome;

                            // Update UI only if balance is visible
                            if (isBalanceVisible) {
                                tvWalletBalance.setText(String.format("NRs. %s", balance));
                                tvSalesAmount.setText(String.format("NRs. %s", todaysIncome));
                            }

                            Toast.makeText(FarmerDashboardActivity.this,
                                    "Wallet refreshed", Toast.LENGTH_SHORT).show();

                        } else {
                            String errorMessage = "Failed to load wallet data";

                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "Wallet error response: " + errorBody);

                                    RefreshWallet.refreshWalletResponse errorResponse =
                                            new Gson().fromJson(errorBody, RefreshWallet.refreshWalletResponse.class);

                                    if (errorResponse != null && errorResponse.getError() != null) {
                                        errorMessage = errorResponse.getError();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing wallet error", e);
                                    errorMessage = "Error: " + response.code();
                                }
                            }

                            Toast.makeText(FarmerDashboardActivity.this,
                                    errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RefreshWallet.refreshWalletResponse> call, Throwable t) {
                        Log.e(TAG, "Wallet refresh failed", t);
                        Toast.makeText(FarmerDashboardActivity.this,
                                "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dashboard when returning to this activity
        if (sessionManager.isLoggedIn()) {
            fetchDashboardData();
        }
    }
}