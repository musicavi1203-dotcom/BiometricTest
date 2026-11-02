package com.example.biometrictest;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private TextView tvResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResults = findViewById(R.id.tvResults);
        Button btnTest = findViewById(R.id.btnTest);

        displayCapabilities();

        btnTest.setOnClickListener(v -> launchBiometricPrompt());
    }

    private void displayCapabilities() {
        BiometricManager bm = BiometricManager.from(this);
        PackageManager pm = getPackageManager();
        StringBuilder sb = new StringBuilder();

        // Hardware presence
        sb.append("=== Hardware ===\n");
        sb.append("Fingerprint sensor: ")
          .append(pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) ? "YES" : "NO")
          .append("\n");
        sb.append("Face recognition: ")
          .append(pm.hasSystemFeature(PackageManager.FEATURE_FACE) ? "YES" : "NO")
          .append("\n");
        sb.append("Iris recognition: ")
          .append(pm.hasSystemFeature(PackageManager.FEATURE_IRIS) ? "YES" : "NO")
          .append("\n\n");

        // BiometricManager checks
        sb.append("=== BiometricManager ===\n");
        sb.append("BIOMETRIC_STRONG: ").append(canAuth(bm, BiometricManager.Authenticators.BIOMETRIC_STRONG)).append("\n");
        sb.append("BIOMETRIC_WEAK:   ").append(canAuth(bm, BiometricManager.Authenticators.BIOMETRIC_WEAK)).append("\n");
        sb.append("DEVICE_CREDENTIAL:").append(canAuth(bm, BiometricManager.Authenticators.DEVICE_CREDENTIAL)).append("\n");

        tvResults.setText(sb.toString());
    }

    private String canAuth(BiometricManager bm, int authenticator) {
        switch (bm.canAuthenticate(authenticator)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return "✅ READY";
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "❌ NO HARDWARE";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "⚠️ HW UNAVAILABLE";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "⚠️ NONE ENROLLED";
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "⚠️ SECURITY UPDATE REQUIRED";
            default:
                return "❓ UNKNOWN";
        }
    }

    private void launchBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        tvResults.append("\n\nPrompt error " + errorCode + ": " + errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        tvResults.append("\n\n✅ Authentication succeeded!");
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        tvResults.append("\n\n❌ Authentication failed.");
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Test")
                .setSubtitle("Testing device capabilities")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
