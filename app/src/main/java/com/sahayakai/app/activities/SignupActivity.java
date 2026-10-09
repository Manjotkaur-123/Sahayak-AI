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
 * SignupActivity — creates a new user account with Supabase Authentication.
 *
 * HOW IT WORKS:
 * - Collects Name, Email, Password, Confirm Password.
 * - Validates inputs.
 * - Calls SupabaseHelper.signUp() → POSTs to Supabase Auth /signup endpoint.
 * - Creates a basic profile row in Supabase with name + email.
 * - Redirects to ProfileActivity to complete profile.
 *
 * NOTE: Supabase free tier has email confirmation disabled by default in new projects.
 * If you enabled "Confirm email" in Supabase Auth settings, users will get a
 * verification email. For a student project, leaving it off is fine.
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private SupabaseHelper supabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        supabase = SupabaseHelper.getInstance();

        // Initialize views
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);

        btnSignup.setOnClickListener(v -> attemptSignup());
        tvLogin.setOnClickListener(v -> finish()); // Go back to Login
    }

    private void attemptSignup() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ?
                etConfirmPassword.getText().toString().trim() : "";

        // Validate
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
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
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        // Create account with Supabase Auth
        supabase.signUp(email, password, new SupabaseHelper.OnAuthCallback() {
            @Override
            public void onSuccess(String userId, String userEmail) {
                // Save basic profile (name + email) so we have a record
                com.sahayakai.app.models.User basicUser = new com.sahayakai.app.models.User();
                basicUser.setName(name);
                basicUser.setEmail(userEmail);

                supabase.saveProfile(basicUser, new SupabaseHelper.OnCompleteCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            setLoading(false);
                            // Go to Profile screen to complete profile
                            Intent intent = new Intent(SignupActivity.this, ProfileActivity.class);
                            intent.putExtra("userName", name);
                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        // Even if profile save partially fails, still go to profile screen
                        runOnUiThread(() -> {
                            setLoading(false);
                            Intent intent = new Intent(SignupActivity.this, ProfileActivity.class);
                            intent.putExtra("userName", name);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(SignupActivity.this,
                            "Sign up failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignup.setEnabled(!loading);
        btnSignup.setText(loading ? "Creating Account..." : "Create Account");
    }
}
