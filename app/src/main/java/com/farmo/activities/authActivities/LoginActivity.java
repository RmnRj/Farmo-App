package com.farmo.activities.authActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.android.material.textfield.TextInputEditText;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private CheckBox cbRememberMe;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. ALWAYS set the layout first
        setContentView(R.layout.activity_login);

        // 2. Initialize the session manager
        sessionManager = new SessionManager(this);

        // 3. Initialize all views
        initViews();

        // 4. Set up the progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing you in...");
        progressDialog.setCancelable(false);

        // 5. Check for auto-login ONLY after views are ready
        if (sessionManager.isLoggedIn()) {
            performTokenLogin();
        }
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        loginButton = findViewById(R.id.btn_login);

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> performLogin());
        }

        TextView forgotPassword = findViewById(R.id.tv_forgot_password);
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, FP_IdentifyUserActivity.class);
                startActivity(intent);
            });
        }

        TextView signUp = findViewById(R.id.tv_signup);
        if (signUp != null) {
            signUp.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }
    }

    private void performTokenLogin() {

        String token = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId();
        String refreshToken = sessionManager.getRefreshToken();
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;

        TokenLoginRequest request = new TokenLoginRequest(token, refreshToken, userId, deviceInfo);

        RetrofitClient.getApiService(this).loginWithToken(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    sessionManager.saveSession(
                            loginResponse.getUserId(),
                            loginResponse.getUserType(),
                            loginResponse.getToken(),
                            loginResponse.getRefreshToken(),
                            true
                    );
                    goToDashboard(loginResponse.getUserId(), loginResponse.getUserType());
                } else {
                    sessionManager.clearSession();
                    Toast.makeText(LoginActivity.this, "Session expired", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                if (loginButton != null) {
                    loginButton.setEnabled(true);
                    loginButton.setAlpha(1.0f);
                }
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin() {
        String identifier = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        boolean rememberMe = cbRememberMe.isChecked();

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;
        LoginRequest loginRequest = new LoginRequest(identifier, password, rememberMe, deviceInfo);

        RetrofitClient.getApiService(this).login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    sessionManager.saveSession(
                            loginResponse.getUserId(),
                            loginResponse.getUserType(),
                            loginResponse.getToken(),
                            loginResponse.getRefreshToken(),
                            rememberMe
                    );
                    goToDashboard(loginResponse.getUserId(), loginResponse.getUserType());
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToDashboard(String userId, String userType) {
        if (userType.equals("Farmer") || userType.equals("VerifiedFarmer")) {
            Intent intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_TYPE", userType);
            startActivity(intent);
            finish(); // ✅ inside the branch
        } else if (userType.equals("Consumer") || userType.equals("VerifiedConsumer")) {
            Intent intent = new Intent(LoginActivity.this, ConsumerDashboardActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_TYPE", userType);
            startActivity(intent);
            finish(); // ✅ inside the branch
        } else {
            Toast.makeText(LoginActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
            // ❌ No finish() here — stay on login screen
        }
    }
}
