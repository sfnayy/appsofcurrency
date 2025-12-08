package com.example.sofcurrency;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.sofcurrency.databinding.ActivityMainBinding;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Format Tampilan (Pemisah Ribuan)
    private DecimalFormat df = new DecimalFormat("#,###.##");

    private String[] currencyCodes = {"IDR", "USD", "EUR", "JPY", "GBP", "SGD", "MYR", "SAR", "KRW", "CNY"};
    private double currentRate = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSpinners();
        setupListeners();
    }

    private void setupSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, currencyCodes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spSourceCurrency.setAdapter(adapter);
        binding.spTargetCurrency.setAdapter(adapter);

        binding.spSourceCurrency.setSelection(0); // IDR
        binding.spTargetCurrency.setSelection(1); // USD
    }

    private void setupListeners() {
        // 1. TOMBOL BAHASA
        binding.btnLanguage.setOnClickListener(v -> showLanguageDialog());

        // 2. INPUT TEXT WATCHER (AUTO FORMAT RIBUAN)
        // Ini adalah logika baru untuk memisahkan angka (1.000.000)
        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Hapus listener sementara agar tidak looping (infinite loop)
                binding.etAmount.removeTextChangedListener(this);

                try {
                    String originalString = s.toString();

                    if (!originalString.isEmpty()) {
                        // 1. Bersihkan format lama (hapus koma/titik)
                        // Kita hapus semua karakter yang BUKAN angka
                        String cleanString = originalString.replaceAll("[,.]", "");

                        // 2. Parsing ke Double
                        double parsed = Double.parseDouble(cleanString);

                        // 3. Format ulang (Contoh: 1000 -> 1,000 atau 1.000)
                        String formatted = df.format(parsed);

                        // 4. Set teks baru ke EditText
                        binding.etAmount.setText(formatted);

                        // 5. Taruh kursor di paling belakang agar enak ngetiknya
                        binding.etAmount.setSelection(formatted.length());
                    }
                } catch (NumberFormatException e) {
                    // Abaikan jika error
                }

                // Pasang listener lagi
                binding.etAmount.addTextChangedListener(this);

                // Hitung hasil konversi
                calculateResult();
            }
        });

        // 3. SAAT MATA UANG DIPILIH (Spinner)
        AdapterView.OnItemSelectedListener currencyChangeListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String source = binding.spSourceCurrency.getSelectedItem().toString();
                String target = binding.spTargetCurrency.getSelectedItem().toString();

                binding.tvSymbolSource.setText(getCurrencySymbol(source));
                binding.tvSymbolTarget.setText(getCurrencySymbol(target));
                binding.imgFlagSource.setImageResource(getFlagResource(source));
                binding.imgFlagTarget.setImageResource(getFlagResource(target));

                updateQuickButtons(source);
                fetchExchangeRate(source, target);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        binding.spSourceCurrency.setOnItemSelectedListener(currencyChangeListener);
        binding.spTargetCurrency.setOnItemSelectedListener(currencyChangeListener);

        // 4. TOMBOL TUKAR (Switch)
        binding.btnSwitch.setOnClickListener(v -> {
            int sourcePos = binding.spSourceCurrency.getSelectedItemPosition();
            int targetPos = binding.spTargetCurrency.getSelectedItemPosition();
            binding.spSourceCurrency.setSelection(targetPos);
            binding.spTargetCurrency.setSelection(sourcePos);
        });

        // 5. TOMBOL CEPAT
        binding.btnQuick1.setOnClickListener(v -> setQuickAmount(1));
        binding.btnQuick2.setOnClickListener(v -> setQuickAmount(10));
        binding.btnQuick3.setOnClickListener(v -> setQuickAmount(100));

        // 6. TOMBOL GRAFIK
        binding.btnViewGraph.setOnClickListener(v -> {
            String source = binding.spSourceCurrency.getSelectedItem().toString();
            String target = binding.spTargetCurrency.getSelectedItem().toString();
            Intent intent = new Intent(MainActivity.this, GraphActivity.class);
            intent.putExtra("SOURCE", source);
            intent.putExtra("TARGET", target);
            startActivity(intent);
        });
    }

    // --- HELPER METHODS ---

    private String getCurrencySymbol(String code) {
        switch (code) {
            case "IDR": return "Rp";
            case "USD": return "$";
            case "EUR": return "€";
            case "JPY": return "¥";
            case "GBP": return "£";
            case "SGD": return "S$";
            case "MYR": return "RM";
            case "SAR": return "﷼";
            case "KRW": return "₩";
            case "CNY": return "¥";
            default: return "";
        }
    }

    private int getFlagResource(String currencyCode) {
        switch (currencyCode) {
            case "IDR": return R.drawable.flag_id;
            case "USD": return R.drawable.flag_us;
            case "EUR": return R.drawable.flag_eu;
            case "JPY": return R.drawable.flag_jp;
            case "GBP": return R.drawable.flag_uk;
            case "SAR": return R.drawable.flag_sa;
            case "KRW": return R.drawable.flag_kr;
            case "CNY": return R.drawable.flag_cn;
            case "SGD": return R.drawable.flag_sg;
            case "MYR": return R.drawable.flag_my;
            default: return R.drawable.flag_us;
        }
    }

    private void updateQuickButtons(String currency) {
        if (currency.equals("IDR")) {
            binding.btnQuick1.setText("Rp 1.000");
            binding.btnQuick2.setText("Rp 10.000");
            binding.btnQuick3.setText("Rp 100.000");
        } else {
            binding.btnQuick1.setText("1");
            binding.btnQuick2.setText("10");
            binding.btnQuick3.setText("100");
        }
    }

    private void setQuickAmount(int multiplier) {
        String currency = binding.spSourceCurrency.getSelectedItem().toString();
        int value = multiplier;
        if (currency.equals("IDR")) {
            value = multiplier * 1000;
        }

        // Kita set teks, TextWatcher akan otomatis memformatnya jadi ribuan
        binding.etAmount.setText(String.valueOf(value));
        binding.etAmount.setSelection(binding.etAmount.getText().length());
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Bahasa Indonesia"};
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_language_title))
                .setItems(languages, (dialog, which) -> {
                    if (which == 0) setLocale("en");
                    else setLocale("in");
                })
                .show();
    }

    private void setLocale(String languageCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    // --- API & KALKULASI ---

    private void fetchExchangeRate(String from, String to) {
        binding.tvRateInfo.setText(getString(R.string.info_loading));
        if (from.equals(to)) {
            currentRate = 1.0;
            updateInfoBox(from, to, 1.0);
            calculateResult();
            return;
        }
        new Thread(() -> {
            try {
                String urlString = "https://api.frankfurter.app/latest?from=" + from + "&to=" + to;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                double rate = jsonResponse.getJSONObject("rates").getDouble(to);
                currentRate = rate;
                runOnUiThread(() -> {
                    updateInfoBox(from, to, rate);
                    calculateResult();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    binding.tvRateInfo.setText(getString(R.string.info_failed));
                    currentRate = 0;
                });
            }
        }).start();
    }

    private void updateInfoBox(String from, String to, double rate) {
        String marketText = getString(R.string.info_market);
        String info;
        if (from.equals("IDR")) {
            double rate1000 = rate * 1000;
            info = "1.000 " + from + " = " + df.format(rate1000) + " " + to + "\n" + marketText;
        } else {
            info = "1 " + from + " = " + df.format(rate) + " " + to + "\n" + marketText;
        }
        binding.tvRateInfo.setText(info);
    }

    private void calculateResult() {
        String inputStr = binding.etAmount.getText().toString();

        if (!inputStr.isEmpty() && currentRate > 0) {
            try {
                // PENTING: Bersihkan dulu format ribuan sebelum dihitung matematika
                String cleanString = inputStr.replaceAll("[,.]", "");

                double amount = Double.parseDouble(cleanString);
                double result = amount * currentRate;

                binding.tvResult.setText(df.format(result));
            } catch (NumberFormatException e) {
                binding.tvResult.setText("0");
            }
        } else {
            binding.tvResult.setText("0");
        }
    }
}