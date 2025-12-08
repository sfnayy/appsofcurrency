package com.example.sofcurrency;

public class CurrencyModel {
    private String code;     // Contoh: "USD"
    private String name;     // Contoh: "Dollar"
    private double rateToIDR; // Nilai tukar ke Rupiah (Base Currency)

    // Constructor (Untuk membuat object baru)
    public CurrencyModel(String code, String name, double rateToIDR) {
        this.code = code;
        this.name = name;
        this.rateToIDR = rateToIDR;
    }

    // Getters (Untuk mengambil data)
    public String getCode() { return code; }
    public String getName() { return name; }
    public double getRateToIDR() { return rateToIDR; }

    // Logic OOP: Mata uang ini bisa menghitung konversinya sendiri ke mata uang lain
    public double convertTo(double amount, CurrencyModel targetCurrency) {
        // Rumus: (Jumlah * RateAsal) / RateTujuan
        double amountInIDR = amount * this.rateToIDR;
        return amountInIDR / targetCurrency.getRateToIDR();
    }
}