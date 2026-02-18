package com.farmo.activities.authActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.farmo.activities.consumerActivities.ConsumerDashboardActivity;
import com.farmo.activities.farmerActivities.FarmerDashboardActivity;
import com.farmo.R;
import com.farmo.network.auth.LoginRequest;
import com.farmo.network.auth.LoginResponse;
import com.farmo.network.RetrofitClient;
import com.farmo.network.auth.TokenLoginRequest;
import com.farmo.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton loginButton;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize session manager
        sessionManager = new SessionManager(this);

        // 2. Set Content View first to inflate the layout
        setContentView(R.layout.activity_login);

        // 3. Initialize Views
        initViews();

        // 4. Setup Progress Dialog for manual login
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        // 5. AUTO-LOGIN: If a session exists in SharedPreferences, try to login automatically
        if (sessionManager.isLoggedIn()) {
            performTokenLogin();
        }
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);

        TextView forgotPassword = findViewById(R.id.tv_forgot_password);
        TextView signUp = findViewById(R.id.tv_signup);

        loginButton.setOnClickListener(v -> performLogin());

        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, FP_IdentifyUserActivity.class));
        });

        signUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    /**
     * Attempts to log in using existing tokens (Auto-login)
     */
    private void performTokenLogin() {
        setLoadingState(true);

        String token = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId();
        String refreshToken = sessionManager.getRefreshToken();
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;

        TokenLoginRequest request = new TokenLoginRequest(token, refreshToken, userId, deviceInfo);

        RetrofitClient.getApiService(this).loginWithToken(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Success: Server validated the old token, go to dashboard
                    handleLoginSuccess(response.body());
                } else {
                    // Failure: Token is invalid/expired. Clear session and stay on Login screen.
                    setLoadingState(false);
                    sessionManager.clearSession();
                    Toast.makeText(LoginActivity.this, "Session expired. Please login.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                // On network error, we don't clear the session (allow retry later)
                Toast.makeText(LoginActivity.this, "Network Error. Check connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Manual login process when user enters username and password
     */
    private void performLogin() {
        String identifier = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);
        progressDialog.show();

        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;
        // Third parameter 'true' signals persistent session to the backend
        LoginRequest loginRequest = new LoginRequest(identifier, password, true, deviceInfo);

        RetrofitClient.getApiService(this).login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    setLoadingState(false);
                    handleLoginError(response, identifier, password);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                setLoadingState(false);
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLoginSuccess(LoginResponse loginResponse) {
        // ALWAYS save the session to SharedPreferences for permanent login
        sessionManager.saveSession(
                loginResponse.getUserId(),
                loginResponse.getUserType(),
                loginResponse.getToken(),
                loginResponse.getRefreshToken(),
                true
        );
        goToDashboard(loginResponse.getUserId(), loginResponse.getUserType());
    }

    private void handleLoginError(Response<LoginResponse> response, String identifier, String password) {
        if (response.errorBody() != null) {
            try {
                String errorBody = response.errorBody().string();
                LoginResponse errorResponse = new Gson().fromJson(errorBody, LoginResponse.class);

                if (response.code() == 403 && errorResponse != null && "ACCOUNT_PENDING".equals(errorResponse.getErrorCode())) {
                    Intent intent = new Intent(LoginActivity.this, Login_ActivateAccountActivity.class);
                    intent.putExtra("USER_ID", identifier);
                    intent.putExtra("CURRENT_PASSWORD", password);
                    startActivity(intent);
                } else {
                    String msg = (errorResponse != null && errorResponse.getError() != null) ? errorResponse.getError() : "Invalid credentials";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setLoadingState(boolean isLoading) {
        if (loginButton != null) {
            loginButton.setEnabled(!isLoading);
            loginButton.setAlpha(isLoading ? 0.5f : 1.0f);
        }
    }

    private void goToDashboard(String userId, String userType) {
        Intent intent;
        if (userType.equalsIgnoreCase("farmer") || userType.equalsIgnoreCase("verifiedfarmer")) {
            intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
        } else if (userType.equalsIgnoreCase("consumer") || userType.equalsIgnoreCase("verifiedconsumer")) {
            intent = new Intent(LoginActivity.this, ConsumerDashboardActivity.class);
        } else {
            Toast.makeText(this, "Invalid user type", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_TYPE", userType);

        // Clear activity stack so user cannot go back to login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}