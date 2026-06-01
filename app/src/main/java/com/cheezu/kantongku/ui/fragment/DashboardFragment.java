package com.cheezu.kantongku.ui.fragment;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cheezu.kantongku.R;
import com.cheezu.kantongku.data.api.ApiClient;
import com.cheezu.kantongku.data.api.ApiResponse;
import com.cheezu.kantongku.data.api.TransaksiApiService;
import com.cheezu.kantongku.ui.adapter.TransaksiAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.cheezu.kantongku.util.NotificationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;


public class DashboardFragment extends Fragment {

    private TextView tvSaldo, tvPemasukan, tvPengeluaran, tvBudgetAmount, tvBudgetStatus;
    private ProgressBar progressBudget;
    private RecyclerView rvTransaksi;
    private TransaksiAdapter adapter;
    private TransaksiApiService apiService;

    // Budget limit (nanti diambil dari SharedPreferences)
    private double budgetLimit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init views
        tvSaldo         = view.findViewById(R.id.tv_saldo);
        tvPemasukan     = view.findViewById(R.id.tv_pemasukan);
        tvPengeluaran   = view.findViewById(R.id.tv_pengeluaran);
        tvBudgetAmount  = view.findViewById(R.id.tv_budget_amount);
        tvBudgetStatus  = view.findViewById(R.id.tv_budget_status);
        progressBudget  = view.findViewById(R.id.progress_budget);
        rvTransaksi     = view.findViewById(R.id.rv_transaksi);

        // Set nama bulan
        TextView tvBulan = view.findViewById(R.id.tv_bulan);
        String namaBulan = new java.text.SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"))
                .format(new java.util.Date());
        tvBulan.setText(namaBulan);

        // Icon lonceng
        view.findViewById(R.id.btn_notification).setOnClickListener(v -> showNotifikasiBottomSheet());

        // Setup RecyclerView
        adapter = new TransaksiAdapter(requireContext());
        rvTransaksi.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransaksi.setAdapter(adapter);


        // Init API service
        apiService = ApiClient.getApiService();
        // Request permission notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

    }

    private void loadDashboardData() {
        // Panggil endpoint GET /api/transaksi/bulan-ini
        apiService.getTransaksiBulanIni().enqueue(new Callback<ApiResponse.DashboardResponse>() {

            @Override
            public void onResponse(@NonNull Call<ApiResponse.DashboardResponse> call,
                                   @NonNull Response<ApiResponse.DashboardResponse> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse.DashboardResponse data = response.body();

                    android.util.Log.d("DASHBOARD", "Data size: " + (data.data != null ? data.data.size() : "null"));
                    android.util.Log.d("DASHBOARD", "Saldo: " + data.saldo);

                    // Format Rupiah
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

                    // Update UI
                    tvSaldo.setText(fmt.format(data.saldo).replace("Rp", "Rp ").replace(",00", ""));
                    tvPemasukan.setText(fmt.format(data.totalPemasukan).replace("Rp", "Rp ").replace(",00", ""));
                    tvPengeluaran.setText(fmt.format(data.totalPengeluaran).replace("Rp", "Rp ").replace(",00", ""));

                    // Update budget progress bar
                    updateBudgetBar(data.totalPengeluaran);

                    // Tampilkan 5 transaksi terbaru saja di dashboard
                    if (data.data != null && !data.data.isEmpty()) {
                        int limit = Math.min(data.data.size(), 5);
                        adapter.setData(data.data.subList(0, limit));
                    } else {
                        adapter.setData(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse.DashboardResponse> call,
                                  @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(),
                        "Gagal terhubung ke server: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBudgetBar(double totalPengeluaran) {
        if (!isAdded() || getContext() == null) return;
        if (budgetLimit <= 0) return;
        int persen = (int) ((totalPengeluaran / budgetLimit) * 100);
        persen = Math.min(persen, 100); // max 100

        progressBudget.setProgress(persen);

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String pengeluaranStr = fmt.format(totalPengeluaran).replace("Rp", "Rp ").replace(",00", "");
        String limitStr       = fmt.format(budgetLimit).replace("Rp", "Rp ").replace(",00", "");
        double sisa           = budgetLimit - totalPengeluaran;
        String sisaStr        = fmt.format(Math.max(sisa, 0)).replace("Rp", "Rp ").replace(",00", "");

        tvBudgetAmount.setText(pengeluaranStr + " / " + limitStr);

        if (persen >= 100) {
            tvBudgetStatus.setText("⚠️ Over budget!");
            tvBudgetStatus.setTextColor(requireContext().getColor(R.color.budget_danger));
        } else if (persen >= 80) {
            tvBudgetStatus.setText(persen + "% terpakai · Hampir habis!");
            tvBudgetStatus.setTextColor(requireContext().getColor(R.color.budget_warning));
        } else {
            tvBudgetStatus.setText(persen + "% terpakai · " + sisaStr + " tersisa");
            tvBudgetStatus.setTextColor(requireContext().getColor(R.color.budget_safe));
        }
        // Cek setting notifikasi
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean notifAktif = prefs.getBoolean(SetelanFragment.KEY_NOTIF_BUDGET, true);
        int threshold = prefs.getInt(SetelanFragment.KEY_THRESHOLD, 80);

        if (notifAktif) {
            if (persen >= 100) {
                NotificationHelper.kirimNotifBudget(requireContext(),
                        "⚠️ Over Budget!",
                        "Pengeluaran kamu sudah melebihi budget limit bulan ini!");
            } else if (persen >= threshold) {
                NotificationHelper.kirimNotifBudget(requireContext(),
                        "Peringatan Budget",
                        "Pengeluaran kamu sudah " + persen + "% dari budget limit!");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ambil budget limit dari SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        budgetLimit = prefs.getFloat(SetelanFragment.KEY_BUDGET_TOTAL, 3900000f);

        loadDashboardData();
    }
    private void showNotifikasiBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_notifikasi, null);
        dialog.setContentView(view);

        RecyclerView rv = view.findViewById(R.id.rv_notifikasi);
        TextView tvEmpty = view.findViewById(R.id.tv_notif_empty);
        TextView btnHapus = view.findViewById(R.id.btn_hapus_notif);

        java.util.List<String[]> notifList = NotificationHelper.getNotifikasi(requireContext());

        if (notifList.isEmpty()) {
            rv.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter() {
                @NonNull
                @Override
                public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(
                        @NonNull ViewGroup parent, int viewType) {
                    View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_notifikasi, parent, false);
                    return new androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {};
                }

                @Override
                public void onBindViewHolder(
                        @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
                    String[] item = notifList.get(position);
                    ((TextView) holder.itemView.findViewById(R.id.tv_notif_waktu)).setText(item[0]);
                    ((TextView) holder.itemView.findViewById(R.id.tv_notif_judul)).setText(item[1]);
                    ((TextView) holder.itemView.findViewById(R.id.tv_notif_pesan)).setText(item[2]);
                }

                @Override
                public int getItemCount() { return notifList.size(); }
            });
        }

        btnHapus.setOnClickListener(v -> {
            NotificationHelper.hapusSemuaNotif(requireContext());
            dialog.dismiss();
            Toast.makeText(requireContext(), "Notifikasi dihapus", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}