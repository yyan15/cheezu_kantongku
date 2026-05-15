package com.cheezu.kantongku.data.api;

import com.cheezu.kantongku.data.api.model.Transaksi;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponse {

    public static class TransaksiList {
        @SerializedName("success") public boolean success;
        @SerializedName("data")    public List<Transaksi> data;
    }

    public static class TransaksiSingle {
        @SerializedName("success") public boolean success;
        @SerializedName("message") public String message;
        @SerializedName("data")    public Transaksi data;
    }

    public static class DashboardResponse {
        @SerializedName("success")           public boolean success;
        @SerializedName("data")              public List<Transaksi> data;
        @SerializedName("total_pengeluaran") public double totalPengeluaran;
        @SerializedName("total_pemasukan")   public double totalPemasukan;
        @SerializedName("saldo")             public double saldo;
    }

    public static class StatistikItem {
        @SerializedName("kategori") public String kategori;
        @SerializedName("total")    public double total;
    }

    public static class StatistikResponse {
        @SerializedName("success") public boolean success;
        @SerializedName("data")    public List<StatistikItem> data;
    }

    public static class GeneralResponse {
        @SerializedName("success") public boolean success;
        @SerializedName("message") public String message;
    }
}