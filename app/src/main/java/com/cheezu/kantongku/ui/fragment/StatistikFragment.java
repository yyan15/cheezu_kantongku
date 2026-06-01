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
import com.cheezu.kantongku.data.api.model.Transaksi;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private TextView toggleBulanan, toggleMingguan;

    private boolean isModeMingguan = false;
    private TransaksiApiService apiService;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private List<Transaksi> semuaTransaksi = new ArrayList<>();
    private ProgressBar progressKesehatan, progressLainnya;
    private TextView tvPctKesehatan, tvPctLainnya;

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
        toggleBulanan      = view.findViewById(R.id.toggle_bulanan);
        toggleMingguan     = view.findViewById(R.id.toggle_mingguan);
        progressKesehatan  = view.findViewById(R.id.progress_kesehatan);
        progressLainnya    = view.findViewById(R.id.progress_lainnya);
        tvPctKesehatan     = view.findViewById(R.id.tv_pct_kesehatan);
        tvPctLainnya       = view.findViewById(R.id.tv_pct_lainnya);


        apiService = ApiClient.getApiService();
        setupBarChart();
        setupToggle();
        loadStatistik();
    }

    // ─── Toggle bulanan / mingguan ───────────────────────────
    private void setupToggle() {
        setToggleStyle(isModeMingguan);

        toggleBulanan.setOnClickListener(v -> {
            if (isModeMingguan) {
                isModeMingguan = false;
                setToggleStyle(false);
                loadStatistik();
            }
        });

        toggleMingguan.setOnClickListener(v -> {
            if (!isModeMingguan) {
                isModeMingguan = true;
                setToggleStyle(true);
                loadStatistikMingguan();
            }
        });
    }

    private void setToggleStyle(boolean mingguan) {
        if (mingguan) {
            toggleMingguan.setBackgroundResource(R.drawable.bg_chip_active);
            toggleMingguan.setTextColor(Color.WHITE);
            toggleBulanan.setBackgroundResource(R.drawable.bg_chip_normal);
            toggleBulanan.setTextColor(Color.parseColor("#64748B"));
        } else {
            toggleBulanan.setBackgroundResource(R.drawable.bg_chip_active);
            toggleBulanan.setTextColor(Color.WHITE);
            toggleMingguan.setBackgroundResource(R.drawable.bg_chip_normal);
            toggleMingguan.setTextColor(Color.parseColor("#64748B"));
        }
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

    // ─── Mode BULANAN ────────────────────────────────────────
    private void loadStatistik() {
        apiService.getStatistik().enqueue(new Callback<ApiResponse.StatistikResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.StatistikResponse> call,
                                   @NonNull Response<ApiResponse.StatistikResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null)
                    updateKategoriUI(response.body().data);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse.StatistikResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(), "Gagal memuat statistik: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getTransaksiBulanIni().enqueue(new Callback<ApiResponse.DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.DashboardResponse> call,
                                   @NonNull Response<ApiResponse.DashboardResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse.DashboardResponse data = response.body();
                    tvTotalPengeluaran.setText(
                            fmt.format(data.totalPengeluaran).replace("Rp", "").replace(",00", "").trim());
                    long jumlahPengeluaran = data.data != null
                            ? data.data.stream().filter(t -> "pengeluaran".equalsIgnoreCase(t.getTipe())).count()
                            : 0;
                    tvJumlahTransaksi.setText(String.valueOf(jumlahPengeluaran));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse.DashboardResponse> call, @NonNull Throwable t) {}
        });

        updateBarChartBulanan();// pindah ke sini, di luar semua callback
    }

    // ─── Mode MINGGUAN ───────────────────────────────────────
    private void loadStatistikMingguan() {
        apiService.getAllTransaksi().enqueue(new Callback<ApiResponse.TransaksiList>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.TransaksiList> call,
                                   @NonNull Response<ApiResponse.TransaksiList> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    semuaTransaksi = response.body().data;
                    hitungStatistikMingguan(semuaTransaksi);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse.TransaksiList> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(), "Gagal memuat data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hitungStatistikMingguan(List<Transaksi> semua) {
        Calendar cal = Calendar.getInstance();

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startMinggu = cal.getTimeInMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        double[] harian = new double[7];
        double totalMakan = 0, totalTransport = 0, totalBelanja = 0,
                totalHiburan = 0, totalKesehatan = 0, totalLainnya = 0, totalMingguIni = 0;

        int jumlah = 0;

        for (Transaksi t : semua) {
            if (!"pengeluaran".equalsIgnoreCase(t.getTipe()) || t.getTanggal() == null) continue;
            try {
                String tanggalBersih = t.getTanggal().length() >= 10
                        ? t.getTanggal().substring(0, 10)
                        : t.getTanggal();
                Date tgl = sdf.parse(tanggalBersih);
                if (tgl == null || tgl.getTime() < startMinggu) continue;

                Calendar c = Calendar.getInstance();
                c.setTime(tgl);
                int dow = c.get(Calendar.DAY_OF_WEEK);
                int idx = (dow == Calendar.SUNDAY) ? 6 : dow - 2; // Senin=0
                if (idx >= 0 && idx < 7) harian[idx] += t.getNominal();

                totalMingguIni += t.getNominal();
                jumlah++;

                switch (t.getKategori()) {
                    case "Makan":     totalMakan     += t.getNominal(); break;
                    case "Transport": totalTransport += t.getNominal(); break;
                    case "Belanja":   totalBelanja   += t.getNominal(); break;
                    case "Hiburan":   totalHiburan   += t.getNominal(); break;
                    case "Kesehatan": totalKesehatan += t.getNominal(); break;
                    case "Lainnya":   totalLainnya   += t.getNominal(); break;
                }
            } catch (ParseException e) { e.printStackTrace(); }
        }

        // Cari kategori terbesar
        double[] totKat = {totalMakan, totalTransport, totalBelanja, totalHiburan, totalKesehatan, totalLainnya};
        String[] nmKat  = {"Makan", "Transport", "Belanja", "Hiburan", "Kesehatan", "Lainnya"};
        String terbesar = "-";
        double max = 0;
        for (int i = 0; i < totKat.length; i++) {
            if (totKat[i] > max) { max = totKat[i]; terbesar = nmKat[i]; }
        }

        tvTotalPengeluaran.setText(
                fmt.format(totalMingguIni).replace("Rp", "").replace(",00", "").trim());
        tvJumlahTransaksi.setText(String.valueOf(jumlah));
        tvKategoriTerbesar.setText(jumlah == 0 ? "-" : terbesar);

        double totalAll = totalMakan + totalTransport + totalBelanja
                + totalHiburan + totalKesehatan + totalLainnya;
        updateProgressKategori(totalMakan, totalTransport, totalBelanja,
                totalHiburan, totalKesehatan, totalLainnya, totalAll);
    }

    private void updateKategoriUI(List<ApiResponse.StatistikItem> items) {
        if (!isAdded() || getContext() == null || items == null || items.isEmpty()) return;

        double totalAll = 0, makan = 0, transport = 0, belanja = 0,
                hiburan = 0, kesehatan = 0, lainnya = 0;
        String terbesar = "-";
        double max = 0;

        for (ApiResponse.StatistikItem item : items) {
            totalAll += item.total;
            if (item.total > max) { max = item.total; terbesar = item.kategori; }
            switch (item.kategori) {
                case "Makan":     makan     = item.total; break;
                case "Transport": transport = item.total; break;
                case "Belanja":   belanja   = item.total; break;
                case "Hiburan":   hiburan   = item.total; break;
                case "Kesehatan": kesehatan = item.total; break;
                case "Lainnya":   lainnya   = item.total; break;
            }
        }
        tvKategoriTerbesar.setText(terbesar);
        updateProgressKategori(makan, transport, belanja, hiburan, kesehatan, lainnya, totalAll);
    }

    private void updateProgressKategori(double makan, double transport,
                                        double belanja, double hiburan,
                                        double kesehatan, double lainnya, double total) {
        if (!isAdded() || getContext() == null) return;
        int pM  = total > 0 ? (int) ((makan      / total) * 100) : 0;
        int pT  = total > 0 ? (int) ((transport  / total) * 100) : 0;
        int pB  = total > 0 ? (int) ((belanja    / total) * 100) : 0;
        int pH  = total > 0 ? (int) ((hiburan    / total) * 100) : 0;
        int pK  = total > 0 ? (int) ((kesehatan  / total) * 100) : 0;
        int pL  = total > 0 ? (int) ((lainnya    / total) * 100) : 0;

        progressMakan.setProgress(pM);
        progressTransport.setProgress(pT);
        progressBelanja.setProgress(pB);
        progressHiburan.setProgress(pH);
        progressKesehatan.setProgress(pK);
        progressLainnya.setProgress(pL);

        tvPctMakan.setText(fmt.format(makan).replace("Rp", "Rp ").replace(",00", "") + " · " + pM + "%");
        tvPctTransport.setText(fmt.format(transport).replace("Rp", "Rp ").replace(",00", "") + " · " + pT + "%");
        tvPctBelanja.setText(fmt.format(belanja).replace("Rp", "Rp ").replace(",00", "") + " · " + pB + "%");
        tvPctHiburan.setText(fmt.format(hiburan).replace("Rp", "Rp ").replace(",00", "") + " · " + pH + "%");
        tvPctKesehatan.setText(fmt.format(kesehatan).replace("Rp", "Rp ").replace(",00", "") + " · " + pK + "%");
        tvPctLainnya.setText(fmt.format(lainnya).replace("Rp", "Rp ").replace(",00", "") + " · " + pL + "%");
    }

    // ─── Bar chart bulanan (6 bulan) ─────────────────────────
    private void updateBarChartBulanan() {
        if (!isAdded() || getContext() == null) return;

        apiService.getAllTransaksi().enqueue(new Callback<ApiResponse.TransaksiList>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.TransaksiList> call,
                                   @NonNull Response<ApiResponse.TransaksiList> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    hitungBarChartBulanan(response.body().data);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse.TransaksiList> call, @NonNull Throwable t) {}
        });
    }

    private void hitungBarChartBulanan(List<Transaksi> semua) {
        if (!isAdded() || getContext() == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar sekarang = Calendar.getInstance();

        // Siapkan 6 bulan terakhir
        double[] totalPerBulan = new double[6];
        String[] labels = new String[6];
        String[] namaBulan = {"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des"};

        // index 5 = bulan ini, index 0 = 5 bulan lalu
        int[] targetBulan = new int[6];
        int[] targetTahun = new int[6];
        Calendar temp = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            targetBulan[i] = temp.get(Calendar.MONTH);
            targetTahun[i] = temp.get(Calendar.YEAR);
            labels[i] = namaBulan[targetBulan[i]];
            temp.add(Calendar.MONTH, -1);
        }

        for (Transaksi t : semua) {
            if (!"pengeluaran".equalsIgnoreCase(t.getTipe()) || t.getTanggal() == null) continue;
            try {
                String tanggalBersih = t.getTanggal().length() >= 10
                        ? t.getTanggal().substring(0, 10) : t.getTanggal();
                Date tgl = sdf.parse(tanggalBersih);
                if (tgl == null) continue;

                Calendar c = Calendar.getInstance();
                c.setTime(tgl);
                int bln = c.get(Calendar.MONTH);
                int thn = c.get(Calendar.YEAR);

                for (int i = 0; i < 6; i++) {
                    if (bln == targetBulan[i] && thn == targetTahun[i]) {
                        totalPerBulan[i] += t.getNominal();
                        break;
                    }
                }
            } catch (ParseException e) { e.printStackTrace(); }
        }

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 6; i++) entries.add(new BarEntry(i, (float) totalPerBulan[i]));

        BarDataSet ds = new BarDataSet(entries, "Pengeluaran");
        ds.setColor(Color.parseColor("#0D9488"));
        ds.setValueTextColor(Color.parseColor("#64748B"));
        ds.setValueTextSize(10f);

        BarData barData = new BarData(ds);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.animateY(800);
        barChart.invalidate();
    }

    // ─── Bar chart mingguan (7 hari) ─────────────────────────
    private void updateBarChartMingguan(double[] harian) {
        if (!isAdded() || getContext() == null) return;
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) entries.add(new BarEntry(i, (float) harian[i]));

        BarDataSet ds = new BarDataSet(entries, "Minggu Ini");
        ds.setColor(Color.parseColor("#0D9488"));
        ds.setValueTextColor(Color.parseColor("#64748B"));
        ds.setValueTextSize(9f);

        BarData barData = new BarData(ds);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);

        barChart.getXAxis().setValueFormatter(
                new IndexAxisValueFormatter(new String[]{"Sen","Sel","Rab","Kam","Jum","Sab","Min"}));
        barChart.animateY(600);
        barChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}