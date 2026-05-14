package com.cheezu.kantongku.data.api;

import com.cheezu.kantongku.data.api.model.Transaksi;
import com.google.gson.annotations.SerializedName;
import java.util.List;

// ─── Response list transaksi ─────────────────────────────────
public class ApiResponse {

    // Generic response untuk list
    public static class TransaksiList {
        @SerializedName("success") public boolean success;
        @SerializedName("com/cheezu/kantongku/data")    public List<Transaksi> data;
    }

    // Response untuk satu transaksi
    public static class TransaksiSingle {
        @SerializedName("success") public boolean success;
        @SerializedName("message") public String message;
        @SerializedName("com/cheezu/kantongku/data")    public Transaksi data;
    }

    // Response untuk dashboard (bulan ini)
    public static class DashboardResponse {
        @SerializedName("success")           public boolean success;
        @SerializedName("com/cheezu/kantongku/data")              public List<Transaksi> data;
        @SerializedName("total_pengeluaran") public double totalPengeluaran;
        @SerializedName("total_pemasukan")   public double totalPemasukan;
        @SerializedName("saldo")             public double saldo;
    }

    // Response untuk statistik per kategori
    public static class StatistikItem {
        @SerializedName("kategori") public String kategori;
        @SerializedName("total")    public double total;
    }

    public static class StatistikResponse {
        @SerializedName("success") public boolean success;
        @SerializedName("com/cheezu/kantongku/data")    public List<StatistikItem> data;
    }

    // Response umum (delete, update)
    public static class GeneralResponse {
        @SerializedName("success") public boolean success;
        @SerializedName("message") public String message;
    }
}