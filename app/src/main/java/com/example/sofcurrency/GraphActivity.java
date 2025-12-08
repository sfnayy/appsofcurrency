package com.example.sofcurrency;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sofcurrency.databinding.ActivityGraphBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GraphActivity extends AppCompatActivity {

    private ActivityGraphBinding binding;
    private String sourceCurr, targetCurr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sourceCurr = getIntent().getStringExtra("SOURCE");
        targetCurr = getIntent().getStringExtra("TARGET");

        if (sourceCurr != null && targetCurr != null) {
            binding.tvGraphTitle.setText(sourceCurr + " \u2192 " + targetCurr);
        }

        binding.btnBack.setOnClickListener(v -> finish());

        fetchHistoricalData();
    }

    private void fetchHistoricalData() {
        // GANTI TEKS MANUAL DENGAN getString()
        binding.tvGraphTitle.setText(getString(R.string.status_loading));

        new Thread(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Calendar cal = Calendar.getInstance();

                String endDate = sdf.format(cal.getTime());
                cal.add(Calendar.DAY_OF_YEAR, -30);
                String startDate = sdf.format(cal.getTime());

                String urlString = "https://api.frankfurter.app/" + startDate + ".." + endDate +
                        "?from=" + sourceCurr + "&to=" + targetCurr;

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());

                if (!jsonResponse.has("rates")) {
                    runOnUiThread(() -> {
                        // GANTI TEKS MANUAL
                        binding.tvGraphTitle.setText(getString(R.string.status_empty));
                        Toast.makeText(this, getString(R.string.status_empty), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                JSONObject rates = jsonResponse.getJSONObject("rates");
                Iterator<String> keys = rates.keys();
                List<String> sortedDates = new ArrayList<>();
                while (keys.hasNext()) sortedDates.add(keys.next());
                Collections.sort(sortedDates);

                List<Entry> entries = new ArrayList<>();
                int index = 0;

                for (String date : sortedDates) {
                    JSONObject dailyRate = rates.getJSONObject(date);
                    if (dailyRate.has(targetCurr)) {
                        double val = dailyRate.getDouble(targetCurr);
                        entries.add(new Entry(index, (float) val));
                        index++;
                    }
                }

                runOnUiThread(() -> {
                    if (entries.isEmpty()) {
                        binding.tvGraphTitle.setText(getString(R.string.status_empty));
                    } else {
                        binding.tvGraphTitle.setText(sourceCurr + " \u2192 " + targetCurr);
                        setupChart(entries, sortedDates);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // GANTI TEKS MANUAL
                    binding.tvGraphTitle.setText(getString(R.string.status_failed));
                    Toast.makeText(GraphActivity.this, getString(R.string.status_check_internet), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void setupChart(List<Entry> entries, List<String> dates) {
        // Bagian setup chart ini tidak berubah karena tidak ada teks hardcoded
        // yang perlu diterjemahkan (angka dan tanggal otomatis).

        LineDataSet dataSet = new LineDataSet(entries, "Nilai Tukar");
        int primaryColor = ContextCompat.getColor(this, R.color.primary_blue);
        dataSet.setColor(primaryColor);
        dataSet.setLineWidth(3f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary_blue_dark));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(primaryColor);
        dataSet.setFillAlpha(40);

        LineData lineData = new LineData(dataSet);
        binding.lineChart.setData(lineData);

        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5, true);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dates.size()) {
                    String originalDate = dates.get(index);
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", Locale.US);
                        return outputFormat.format(inputFormat.parse(originalDate));
                    } catch (Exception e) {
                        return originalDate;
                    }
                }
                return "";
            }
        });

        YAxis leftAxis = binding.lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setAxisMinimum(dataSet.getYMin() * 0.999f);
        leftAxis.setAxisMaximum(dataSet.getYMax() * 1.001f);

        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value < 0.01) return String.format(Locale.US, "%.5f", value);
                return String.format(Locale.US, "%.2f", value);
            }
        });

        binding.lineChart.getAxisRight().setEnabled(false);
        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.setTouchEnabled(true);
        binding.lineChart.setPinchZoom(true);
        binding.lineChart.animateX(1000);
        binding.lineChart.setExtraBottomOffset(10f);

        binding.lineChart.invalidate();
    }
}