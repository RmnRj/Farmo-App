package com.farmo.activities.authActivities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.farmo.R;
import com.farmo.network.auth.ForgotPasswordRequest;
import com.farmo.network.auth.ForgotPasswordResponse;
import com.farmo.network.MessageResponse;
import com.farmo.network.RetrofitClient;
import com.farmo.network.auth.VerifyEmailRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FP_ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmailInput;
    private TextInputLayout tilEmailInput;
    private Button btnAction;
    private ProgressDialog progressDialog;
    private TextView tvInstruction, tvEmailDisplay;
    
    private String userId;
    private boolean isEmailVerificationStep = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmailInput = findViewById(R.id.etEmail);
        tilEmailInput = findViewById(R.id.tilEmailInput);
        btnAction = findViewById(R.id.btnSendOtp);
        LinearLayout btnBack = findViewById(R.id.btnBack);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvEmailDisplay = findViewById(R.id.tvEmail);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        btnBack.setOnClickListener(v -> finish());

        // Check if we came from IdentifyUserActivity with data
        String receivedHalfEmail = getIntent().getStringExtra("HALF_EMAIL");
        userId = getIntent().getStringExtra("USER_ID");

        if (receivedHalfEmail != null && userId != null) {
            setupEmailVerificationUI(receivedHalfEmail);
        }

        btnAction.setOnClickListener(v -> {
            String input = Objects.requireNonNull(etEmailInput.getText()).toString().trim();
            if (input.isEmpty()) {
                etEmailInput.setError("Field cannot be empty");
                return;
            }

            if (!isEmailVerificationStep) {
                identifyUser(input);
            } else {
                verifyEmailAndSendOtp(input);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupEmailVerificationUI(String halfEmail) {
        isEmailVerificationStep = true;
        tvEmailDisplay.setVisibility(View.VISIBLE);
        tvEmailDisplay.setText("Email: " + halfEmail);
        tvInstruction.setText("Please enter your full email address to verify your identity.");
        etEmailInput.setText("");
        tilEmailInput.setHint("Full Email Address");
        btnAction.setText("Verify Email & Send OTP");
    }

    private void identifyUser(String identifier) {
        progressDialog.show();
        ForgotPasswordRequest request = new ForgotPasswordRequest(identifier);
        
        RetrofitClient.getApiService(this).forgotPassword(request).enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForgotPasswordResponse> call, @NonNull Response<ForgotPasswordResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    userId = response.body().getUserId();
                    String halfEmail = response.body().getHalfEmail();
                    setupEmailVerificationUI(halfEmail);
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForgotPasswordResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(FP_ForgotPasswordActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyEmailAndSendOtp(String email) {
        progressDialog.show();
        VerifyEmailRequest request = new VerifyEmailRequest(userId, email);
        
        RetrofitClient.getApiService(this).verifyEmail(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(FP_ForgotPasswordActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FP_ForgotPasswordActivity.this, FP_VerifyOtpActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(FP_ForgotPasswordActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String errorBody = response.errorBody().string();
                MessageResponse errorResponse = new Gson().fromJson(errorBody, MessageResponse.class);
                String msg = (errorResponse != null && errorResponse.getError() != null) 
                        ? errorResponse.getError() 
                        : "Error: " + response.code();
                Toast.makeText(FP_ForgotPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(FP_ForgotPasswordActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(FP_ForgotPasswordActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }
}
