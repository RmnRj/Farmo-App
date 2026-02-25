package com.farmo.activities.authActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etUsername, etPassword;
    private boolean rememberMe = false;
    private CheckBox cbRememberMe ;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Initialize progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        initViews();
        if (sessionManager.isLoggedIn()) {
            performTokenLogin();
        }

    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);

        // Standard Login Button click
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

        if (token.isEmpty() || userId.isEmpty()) {
            sessionManager.clearSession();
            return;
        }

        progressDialog.setMessage("Auto-logging in...");
        progressDialog.show();

        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;
        TokenLoginRequest request = new TokenLoginRequest(token, refreshToken, userId, deviceInfo);

        RetrofitClient.getApiService(this).loginWithToken(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (progressDialog.isShowing()) progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // CRITICAL CHANGE: Always pass 'true' as the last parameter
                    // because we are already in an active session.
                    sessionManager.saveSession(
                            loginResponse.getUserId(),
                            loginResponse.getUserType(),
                            loginResponse.getToken(),
                            loginResponse.getRefreshToken(),
                            true
                    );

                    goToDashboard(loginResponse.getUserId(), loginResponse.getUserType());
                } else {
                    // If the token is rejected, we MUST clear everything so the
                    // user is forced back to manual login.
                    sessionManager.clearSession();
                    Toast.makeText(LoginActivity.this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Log.e(TAG, "Token login failed", t);
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin() {
        String identifier = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;
        // Notice: rememberMe is now hardcoded to 'true' to ensure session is always saved
        LoginRequest loginRequest = new LoginRequest(identifier, password, false, deviceInfo);

        RetrofitClient.getApiService(this).login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (progressDialog.isShowing()) progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Always save the session on a successful login
                    sessionManager.saveSession(
                            loginResponse.getUserId(),
                            loginResponse.getUserType(),
                            loginResponse.getToken(),
                            loginResponse.getRefreshToken(),
                            true // Auto-login is enabled by default
                    );
                    //Toast.makeText(LoginActivity.this, loginResponse.getUserId() " ", Toast.LENGTH_SHORT).show();
                    goToDashboard(loginResponse.getUserId(), loginResponse.getUserType());
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Log.e(TAG, "Login failed", t);
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToDashboard(String userId, String userType) {
        Intent intent;
        if (userType.equals("Farmer") || userType.equals("VerifiedFarmer")) {
            intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
        } else if (userType.equals("Consumer") || userType.equals("VerifiedConsumer")) {
            intent = new Intent(LoginActivity.this, ConsumerDashboardActivity.class);
        } else {
            Toast.makeText(LoginActivity.this, "Invalid user type: " + userType, Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_TYPE", userType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
