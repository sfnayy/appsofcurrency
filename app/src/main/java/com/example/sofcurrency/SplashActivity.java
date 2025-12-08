package com.example.sofcurrency;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sofcurrency.databinding.ActivitySplashBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (checkLocationPermission()) {
            getLocationAndAdapt();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndAdapt() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            adaptUI(location.getLatitude(), location.getLongitude());
                        } else {
                            proceedToMain(2000);
                        }
                    }
                });
    }

    private void adaptUI(double lat, double lon) {

        String welcomeText = "Welcome to SOF Currency";
        int loadingColor = Color.WHITE;
        int flagResource = R.drawable.flag_us; // Default Global

        // 1. INDONESIA
        if (lat > -11 && lat < 6 && lon > 95 && lon < 141) {
            welcomeText = "Selamat Datang di SOF Currency";
            loadingColor = Color.parseColor("#FF5252");
            flagResource = R.drawable.flag_id;
        }

        // 2. AMERIKA SERIKAT
        else if (lat > 24 && lat < 50 && lon > -125 && lon < -66) {
            welcomeText = "Welcome to SOF Currency";
            loadingColor = Color.WHITE;
            flagResource = R.drawable.flag_us;
        }

        // 3. EROPA
        else if (lat > 36 && lat < 70 && lon > -10 && lon < 30) {
            welcomeText = "Willkommen bei SOF Currency";
            loadingColor = Color.parseColor("#FFD700");
            flagResource = R.drawable.flag_eu;
        }

        // 4. JEPANG
        else if (lat > 30 && lat < 46 && lon > 128 && lon < 146) {
            welcomeText = "ようこそ SOF Currency へ"; // Format: Youkoso (Welcome) ...
            loadingColor = Color.parseColor("#F44336");
            flagResource = R.drawable.flag_jp;
        }

        // 5. INGGRIS
        else if (lat > 50 && lat < 60 && lon > -8 && lon < 2) {
            welcomeText = "Welcome to SOF Currency UK";
            loadingColor = Color.parseColor("#EF5350");
            flagResource = R.drawable.flag_uk;
        }

        // 6. ARAB SAUDI
        else if (lat > 16 && lat < 32 && lon > 34 && lon < 56) {
            welcomeText = "مرحباً بكم في SOF Currency";
            loadingColor = Color.parseColor("#69F0AE");
            flagResource = R.drawable.flag_sa;
        }

        // 7. KOREA SELATAN
        else if (lat > 33 && lat < 39 && lon > 124 && lon < 131) {
            welcomeText = "환영합니다 SOF Currency";
            loadingColor = Color.parseColor("#FF4081");
            flagResource = R.drawable.flag_kr;
        }

        // 8. CHINA
        else if (lat > 18 && lat < 54 && lon > 73 && lon < 135) {
            welcomeText = "你好 SOF Currency";
            loadingColor = Color.parseColor("#FFAB00");
            flagResource = R.drawable.flag_cn;
        }

        // 9. SINGAPURA
        else if (lat > 1 && lat < 2 && lon > 103 && lon < 104) {
            welcomeText = "Welcome to SOF Currency";
            loadingColor = Color.WHITE;
            flagResource = R.drawable.flag_sg;
        }

        // 10. MALAYSIA
        else if (lat > 1 && lat < 7 && lon > 100 && lon < 119) {
            welcomeText = "Selamat Datang di SOF Currency";
            loadingColor = Color.parseColor("#FFD700");
            flagResource = R.drawable.flag_my;
        }

        // --- UPDATE UI ---
        binding.rootLayout.setBackgroundColor(Color.parseColor("#0D47A1")); // Deep Blue
        binding.tvWelcome.setText(welcomeText);
        binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(loadingColor));
        binding.imgSplashFlag.setImageResource(flagResource); // Update Bendera

        proceedToMain(2500);
    }

    private void proceedToMain(int delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, delayMillis);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getLocationAndAdapt();
    }
}