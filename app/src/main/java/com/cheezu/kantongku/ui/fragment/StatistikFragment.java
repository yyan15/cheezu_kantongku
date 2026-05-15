package com.cheezu.kantongku.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cheezu.kantongku.R;
import com.cheezu.kantongku.data.api.ApiClient;
import com.cheezu.kantongku.data.api.ApiResponse;
import com.cheezu.kantongku.data.api.TransaksiApiService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatistikFragment extends Fragment {

    private BarChart barChart;
    private TextView tvTotalPengeluaran, tvKategoriTerbesar, tvJumlahTransaksi;
    private ProgressBar progressMakan, progressTransport, progressBelanja, progressHiburan;
    private TextView tvPctMakan, tvPctTransport, tvPctBelanja, tvPctHiburan;

    private TransaksiApiService apiService;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistik, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChart           = view.findViewById(R.id.bar_chart);
        tvTotalPengeluaran = view.findViewById(R.id.tv_total_pengeluaran);
        tvKategoriTerbesar = view.findViewById(R.id.tv_kategori_terbesar);
        tvJumlahTransaksi  = view.findViewById(R.id.tv_jumlah_transaksi);
        progressMakan      = view.findViewById(R.id.progress_makan);
        progressTransport  = view.findViewById(R.id.progress_transport);
        progressBelanja    = view.findViewById(R.id.progress_belanja);
        progressHiburan    = view.findViewById(R.id.progress_hiburan);
        tvPctMakan         = view.findViewById(R.id.tv_pct_makan);
        tvPctTransport     = view.findViewById(R.id.tv_pct_transport);
        tvPctBelanja       = view.findViewById(R.id.tv_pct_belanja);
        tvPctHiburan       = view.findViewById(R.id.tv_pct_hiburan);

        apiService = ApiClient.getApiService();
        setupBarChart();
        loadStatistik();
    }

    private void setupBarChart() {
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.animateY(800);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#64748B"));

        barChart.getAxisLeft().setTextColor(Color.parseColor("#64748B"));
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisRight().setEnabled(false);
    }

    private void loadStatistik() {
        apiService.getStatistik().enqueue(new Callback<ApiResponse.StatistikResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.StatistikResponse> call,
                                   @NonNull Response<ApiResponse.StatistikResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    updateStatistikUI(response.body().data);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse.StatistikResponse> call,
                                  @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(),
                        "Gagal memuat statistik: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getTransaksiBulanIni().enqueue(new Callback<ApiResponse.DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.DashboardResponse> call,
                                   @NonNull Response<ApiResponse.DashboardResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse.DashboardResponse data = response.body();
                    String total = fmt.format(data.totalPengeluaran)
                            .replace("Rp", "").replace(",00", "").trim();
                    tvTotalPengeluaran.setText(total);
                    tvJumlahTransaksi.setText(
                            String.valueOf(data.data != null ? data.data.size() : 0));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse.DashboardResponse> call,
                                  @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
            }
        });
    }

    private void updateStatistikUI(List<ApiResponse.StatistikItem> items) {
        if (!isAdded() || getContext() == null || items == null || items.isEmpty()) return;

        double totalAll = 0;
        String kategoriTerbesar = "-";
        double maxNominal = 0;

        for (ApiResponse.StatistikItem item : items) {
            totalAll += item.total;
            if (item.total > maxNominal) {
                maxNominal = item.total;
                kategoriTerbesar = item.kategori;
            }
        }

        tvKategoriTerbesar.setText(kategoriTerbesar);

        for (ApiResponse.StatistikItem item : items) {
            int persen = totalAll > 0 ? (int) ((item.total / totalAll) * 100) : 0;
            String label = fmt.format(item.total).replace("Rp", "Rp ")
                    .replace(",00", "") + " · " + persen + "%";

            switch (item.kategori) {
                case "Makan":     progressMakan.setProgress(persen);     tvPctMakan.setText(label);     break;
                case "Transport": progressTransport.setProgress(persen); tvPctTransport.setText(label); break;
                case "Belanja":   progressBelanja.setProgress(persen);   tvPctBelanja.setText(label);   break;
                case "Hiburan":   progressHiburan.setProgress(persen);   tvPctHiburan.setText(label);   break;
            }
        }

        updateBarChart();
    }

    private void updateBarChart() {
        if (!isAdded() || getContext() == null) return;
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 1500000));
        entries.add(new BarEntry(1, 2200000));
        entries.add(new BarEntry(2, 1800000));
        entries.add(new BarEntry(3, 3100000));
        entries.add(new BarEntry(4, 2400000));
        entries.add(new BarEntry(5, 2660000));

        BarDataSet dataSet = new BarDataSet(entries, "Pengeluaran");
        dataSet.setColor(Color.parseColor("#0D9488"));
        dataSet.setValueTextColor(Color.parseColor("#64748B"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);

        String[] bulan = {"Des", "Jan", "Feb", "Mar", "Apr", "Mei"};
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(bulan));
        barChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistik();
    }
}