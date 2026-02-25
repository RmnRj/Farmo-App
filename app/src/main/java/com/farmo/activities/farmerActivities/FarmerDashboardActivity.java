package com.farmo.activities.farmerActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.widget.NestedScrollView;

import com.farmo.activities.authActivities.LoginActivity;
import com.farmo.R;
import com.farmo.activities.commonActivities.ProfileActivity;
import com.farmo.network.Dashboard.DashboardService;
import com.farmo.network.Dashboard.RefreshWallet;
import com.farmo.network.RetrofitClient;
import com.farmo.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "FarmerDashboard";

    private boolean isBalanceVisible = true;
    private String walletBalance = "0.00";
    private String fullName = "UserName";
    private String todaySales = "0.00";

    private SessionManager sessionManager;
    private TextView tvSalesAmount, tvWalletBalance;

    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isManualRefresh = false;

    private RelativeLayout loadingOverlay;
    private NestedScrollView mainScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        sessionManager = new SessionManager(this);
        mainScrollView = findViewById(R.id.nested_scroll_view);

        // FIX 1: Check login BEFORE doing anything else
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupUI();
        setupSwipeRefresh();
        fetchDashboardData();
    }

    // FIX 2: onResume guard — prevent double-fetch on first launch
    @Override
    protected void onResume() {
        super.onResume();

        // Check if the user is still logged in
        if (sessionManager.isLoggedIn()) {
            // Refresh all dashboard data (Wallet, Orders, Market Prices)
            // This ensures data is current even if they just came back from another app
            fetchDashboardData();
        } else {
            // If session was cleared (e.g., token expired in background), kick to login
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        sessionManager.clearSession();
        Intent intent = new Intent(FarmerDashboardActivity.this, LoginActivity.class); // Explicit context
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoadingOverlay(boolean show) {
        if (show) {
            if (loadingOverlay == null) {
                loadingOverlay = new RelativeLayout(this);
                loadingOverlay.setLayoutParams(new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                loadingOverlay.setBackgroundColor(Color.parseColor("#99000000"));
                loadingOverlay.setClickable(true);
                loadingOverlay.setFocusable(true);

                ProgressBar progressBar = new ProgressBar(this);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                progressBar.setLayoutParams(lp);
                progressBar.setIndeterminateTintList(
                        ColorStateList.valueOf(Color.WHITE));

                loadingOverlay.addView(progressBar);

                ViewGroup rootView = findViewById(android.R.id.content);
                rootView.addView(loadingOverlay);
            }
            loadingOverlay.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && mainScrollView != null) {
                mainScrollView.setRenderEffect(
                        RenderEffect.createBlurEffect(15f, 15f, Shader.TileMode.CLAMP));
            }
        } else {
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && mainScrollView != null) {
                mainScrollView.setRenderEffect(null);
            }
        }
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        if (swipeRefreshLayout == null) return;

        swipeRefreshLayout.setColorSchemeResources(
                R.color.topical_forest, android.R.color.holo_green_dark);

        if (mainScrollView != null) {
            mainScrollView.setOnScrollChangeListener(
                    (NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                        swipeRefreshLayout.setEnabled(scrollY == 0);
                    });
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            isManualRefresh = true;
            fetchDashboardData();
        });
    }

    private void fetchDashboardData() {
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Show overlay only on first load (data not yet fetched)
        if (!isManualRefresh && walletBalance.equals("0.00")) {
            showLoadingOverlay(true);
        } else if (isManualRefresh && swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        RetrofitClient.getApiService(this)
                .getDashboard(sessionManager.getAuthToken(), sessionManager.getUserId())
                .enqueue(new Callback<DashboardService.DashboardResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DashboardService.DashboardResponse> call,
                                           @NonNull Response<DashboardService.DashboardResponse> response) {

                        showLoadingOverlay(false);
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            DashboardService.DashboardResponse data = response.body();

                            walletBalance = data.getWallet_amt() != null ? data.getWallet_amt() : "0.00";
                            todaySales = data.getTodayIncome() != null ? data.getTodayIncome() : "0.00";
                            fullName = data.getUsername() != null ? data.getUsername() : "User";

                            // FIX 3: Always run UI updates on main thread
                            runOnUiThread(() -> {
                                updateGreeting(fullName);
                                if (isBalanceVisible) {
                                    tvWalletBalance.setText(String.format("NRs. %s", walletBalance));
                                    tvSalesAmount.setText(String.format("NRs. %s", todaySales));
                                }
                            });

                            if (isManualRefresh) {
                                isManualRefresh = false;
                                Toast.makeText(FarmerDashboardActivity.this,
                                        "Refreshed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            handleError(response);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DashboardService.DashboardResponse> call,
                                          @NonNull Throwable t) {
                        showLoadingOverlay(false);
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        isManualRefresh = false;
                        Log.e(TAG, "Dashboard fetch failed", t);
                        Toast.makeText(FarmerDashboardActivity.this,
                                "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleError(Response<DashboardService.DashboardResponse> response) {
        isManualRefresh = false;
        if (response.code() == 401 || response.code() == 403) {
            // FIX 4: Clear session and use FLAG_ACTIVITY_CLEAR_TASK to avoid back-stack issues
            redirectToLogin();
        } else {
            Toast.makeText(this, "Failed to load data (Error " + response.code() + ")",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void setupUI() {
        // FIX 5: Null-check all views before use to prevent NPE crashes
        ImageView ivVisibility = findViewById(R.id.ivVisibility);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvSalesAmount = findViewById(R.id.tvSalesAmount);

        if (tvWalletBalance == null || tvSalesAmount == null || ivVisibility == null) {
            Log.e(TAG, "Critical views not found in layout — check your XML IDs");
            return;
        }

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

        // FIX 6: Null-check click targets (btnProfile is a TextView in XML, not a Button)
        View btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
        }

        // FIX 7: ivRefresh is a RelativeLayout in XML — cast correctly
        View ivRefresh = findViewById(R.id.ivRefresh);
        if (ivRefresh != null) {
            ivRefresh.setOnClickListener(v -> refreshWalletUI());
        }

        findViewById(R.id.idOrderAnalystics).setOnClickListener(v -> 
            gotoFarmerOrderManagement());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_orders) {
                gotoFarmerOrderManagement();
                return true;
            } else if (id == R.id.navigation_home) {
                // Handle Home click
                return true;
            } else if (id == R.id.navigation_products) {
                // Handle Products click
                return true;
            }

            return false;
        });


    }
    
    private void gotoFarmerOrderManagement(){
        Intent intent = new Intent(this, FarmerOrderManagementActivity.class);
        startActivity(intent);
    }
    @SuppressLint("SetTextI18n")
    public void updateGreeting(String name) {
        int timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (timeOfDay < 12) ? "Good Morning"
                : (timeOfDay < 16) ? "Good Afternoon"
                : (timeOfDay < 21) ? "Good Evening"
                : "Good Night";
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        if (tvGreeting != null) {
            tvGreeting.setText(greeting + ", " + name);
        }
    }

    private void refreshWalletUI() {
        Log.d(TAG, "Refreshing wallet data...");

        RetrofitClient.getApiService(this)
                .getRefreshWallet(sessionManager.getAuthToken(), sessionManager.getUserId())
                .enqueue(new Callback<RefreshWallet.refreshWalletResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<RefreshWallet.refreshWalletResponse> call,
                                           @NonNull Response<RefreshWallet.refreshWalletResponse> response) {

                        Log.d(TAG, "Wallet refresh response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            RefreshWallet.refreshWalletResponse data = response.body();

                            String balance = data.getBalance() != null ? data.getBalance() : "0.00";
                            String todayIncome = data.getTodaysIncome() != null
                                    ? data.getTodaysIncome() : "0.00";

                            walletBalance = balance;
                            todaySales = todayIncome;

                            if (isBalanceVisible) {
                                tvWalletBalance.setText(String.format("NRs. %s", balance));
                                tvSalesAmount.setText(String.format("NRs. %s", todayIncome));
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
                                            new Gson().fromJson(errorBody,
                                                    RefreshWallet.refreshWalletResponse.class);
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
                    public void onFailure(@NonNull Call<RefreshWallet.refreshWalletResponse> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "Wallet refresh failed", t);
                        Toast.makeText(FarmerDashboardActivity.this,
                                "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}