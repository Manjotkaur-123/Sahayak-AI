package com.sahayakai.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.sahayakai.app.R;
import com.sahayakai.app.helpers.SupabaseHelper;

/**
 * LoginActivity — handles user login with Supabase Authentication.
 *
 * HOW IT WORKS:
 * - User enters email and password.
 * - We call SupabaseHelper.signIn() which POSTs to Supabase Auth REST API.
 * - On success, the JWT token is saved in SharedPreferences automatically.
 * - We then check if the profile exists → route to Home or Profile.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvForgotPassword;
    private ProgressBar progressBar;

    private SupabaseHelper supabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        supabase = SupabaseHelper.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);

        // Login button
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Go to Signup
        tvSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> sendPasswordReset());
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validate fields
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Show loading
        setLoading(true);

        // Supabase login
        supabase.signIn(email, password, new SupabaseHelper.OnAuthCallback() {
            @Override
            public void onSuccess(String userId, String userEmail) {
                // Login successful — check if profile is complete
                supabase.checkProfileExists(exists -> {
                    runOnUiThread(() -> {
                        setLoading(false);
                        if (exists) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        }
                        finish();
                    });
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this,
                            "Login failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void sendPasswordReset() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email to reset password");
            etEmail.requestFocus();
            return;
        }

        supabase.sendPasswordReset(email, new SupabaseHelper.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent. Check your inbox.",
                                Toast.LENGTH_LONG).show());
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this,
                                "Failed to send reset email. Check the email address.",
                                Toast.LENGTH_LONG).show());
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Logging in..." : getString(R.string.login));
    }
}
