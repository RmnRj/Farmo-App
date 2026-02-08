package com.farmo.activities.farmerActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    // ADD SwipeRefreshLayout
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        sessionManager = new SessionManager(this);
        UserType = sessionManager.getUserType();

        setupUI();
        setupSwipeRefresh(); // ADD this
        fetchDashboardData();
    }

    // ADD this method
    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        // Use your existing colors
        swipeRefreshLayout.setColorSchemeResources(
                R.color.topical_forest,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDashboard();
            }
        });
    }

    // ADD this method
    private void refreshDashboard() {
        // Refresh both dashboard and wallet data
        fetchDashboardData();
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

        // Navigation to Profile
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Quick Actions Grid
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

        // Stats Row
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
        RetrofitClient.getApiService(this).getDashboard().enqueue(new Callback<DashboardService.DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardService.DashboardResponse> call,
                                   Response<DashboardService.DashboardResponse> response) {

                // STOP refresh animation
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    DashboardService.DashboardResponse data = response.body();

                    // Save values safely
                    fullName = data.getUsername() != null ? data.getUsername() : "User";
                    walletBalance = data.getWallet_amt() != null ? data.getWallet_amt() : "0.00";
                    todaySales = data.getTodayIncome() != null ? data.getTodayIncome() : "0.00";

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        updateGreeting(fullName);
                        tvWalletBalance.setText(String.format("NRs. %s", walletBalance));
                        tvSalesAmount.setText(String.format("NRs. %s", todaySales));
                    });

                    // Show success message only on manual refresh
                    if (swipeRefreshLayout != null) {
                        Toast.makeText(FarmerDashboardActivity.this,
                                "Dashboard refreshed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FarmerDashboardActivity.this,
                            "Failed to load dashboard data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardService.DashboardResponse> call, Throwable t) {
                // STOP refresh animation
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                Toast.makeText(FarmerDashboardActivity.this,
                        "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        RetrofitClient.getApiService(this).getRefreshWallet()
                .enqueue(new Callback<RefreshWallet.refreshWalletResponse>() {
                    @Override
                    public void onResponse(Call<RefreshWallet.refreshWalletResponse> call,
                                           Response<RefreshWallet.refreshWalletResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            RefreshWallet.refreshWalletResponse data = response.body();

                            String balance = data.getBalance() != null ? data.getBalance() : "0.00";
                            String todaysIncome = data.getTodaysIncome() != null ? data.getTodaysIncome() : "0.00";

                            // Update local variables
                            walletBalance = balance;
                            todaySales = todaysIncome;

                            // Update UI
                            tvWalletBalance.setText(String.format("NRs. %s", balance));
                            tvSalesAmount.setText(String.format("NRs. %s", todaysIncome));

                            Toast.makeText(FarmerDashboardActivity.this,
                                    "Wallet refreshed", Toast.LENGTH_SHORT).show();

                        } else {
                            String errorMessage = "Failed to load wallet data";

                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    RefreshWallet.refreshWalletResponse errorResponse =
                                            new Gson().fromJson(errorBody, RefreshWallet.refreshWalletResponse.class);

                                    if (errorResponse != null && errorResponse.getError() != null) {
                                        errorMessage = errorResponse.getError();
                                    }
                                } catch (Exception e) {
                                    errorMessage = "Error: " + response.code();
                                }
                            }

                            Toast.makeText(FarmerDashboardActivity.this,
                                    errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RefreshWallet.refreshWalletResponse> call, Throwable t) {
                        Toast.makeText(FarmerDashboardActivity.this,
                                "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void set_walletBalance(String walletBalance) {
        this.walletBalance = walletBalance;
    }

    private String get_walletBalance() {
        return walletBalance;
    }
}