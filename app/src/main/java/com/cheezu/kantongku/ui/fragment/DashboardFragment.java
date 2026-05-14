package com.cheezu.kantongku.ui.fragment;

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

public class DashboardFragment extends Fragment {

    private TextView tvSaldo, tvPemasukan, tvPengeluaran, tvBudgetAmount, tvBudgetStatus;
    private ProgressBar progressBudget;
    private RecyclerView rvTransaksi;
    private TransaksiAdapter adapter;
    private TransaksiApiService apiService;

    // Budget limit (nanti diambil dari SharedPreferences)
    private double budgetLimit = 3900000;

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

        // Setup RecyclerView
        adapter = new TransaksiAdapter(requireContext());
        rvTransaksi.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransaksi.setAdapter(adapter);

        // Init API service
        apiService = ApiClient.getApiService();

        // Load data dari Laravel
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Panggil endpoint GET /api/transaksi/bulan-ini
        apiService.getTransaksiBulanIni().enqueue(new Callback<ApiResponse.DashboardResponse>() {

            @Override
            public void onResponse(@NonNull Call<ApiResponse.DashboardResponse> call,
                                   @NonNull Response<ApiResponse.DashboardResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse.DashboardResponse data = response.body();

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
                Toast.makeText(requireContext(),
                        "Gagal terhubung ke server: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBudgetBar(double totalPengeluaran) {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data setiap kali fragment ditampilkan
        loadDashboardData();
    }
}
