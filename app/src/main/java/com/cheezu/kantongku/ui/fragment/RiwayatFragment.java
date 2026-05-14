package com.cheezu.kantongku.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cheezu.kantongku.R;
import com.cheezu.kantongku.data.api.ApiClient;
import com.cheezu.kantongku.data.api.ApiResponse;
import com.cheezu.kantongku.data.api.TransaksiApiService;
import com.cheezu.kantongku.data.api.model.Transaksi;
import com.cheezu.kantongku.ui.adapter.TransaksiAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiwayatFragment extends Fragment {

    private RecyclerView rvRiwayat;
    private EditText etSearch;
    private TextView chipSemua, chipMakan, chipTransport,
            chipBelanja, chipHiburan;

    private TransaksiAdapter adapter;
    private TransaksiApiService apiService;
    private List<Transaksi> allData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_riwayat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRiwayat      = view.findViewById(R.id.rv_riwayat);
        etSearch       = view.findViewById(R.id.et_search);
        chipSemua      = view.findViewById(R.id.chip_semua);
        chipMakan      = view.findViewById(R.id.chip_makan);
        chipTransport  = view.findViewById(R.id.chip_transport);
        chipBelanja    = view.findViewById(R.id.chip_belanja);
        chipHiburan    = view.findViewById(R.id.chip_hiburan);

        adapter = new TransaksiAdapter(requireContext());
        rvRiwayat.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRiwayat.setAdapter(adapter);

        apiService = ApiClient.getApiService();

        // Klik item → bisa edit (dikembangkan nanti)
        adapter.setOnItemClickListener(new TransaksiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaksi transaksi) {
                // TODO: buka dialog edit
                Toast.makeText(requireContext(),
                        "Edit: " + transaksi.getJudul(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(Transaksi transaksi) {
                showDeleteDialog(transaksi);
            }
        });

        setupSearch();
        setupChipFilter();
        loadSemuaTransaksi();
    }

    // ─── Load semua transaksi dari API ───────────────────────
    private void loadSemuaTransaksi() {
        apiService.getAllTransaksi().enqueue(new Callback<ApiResponse.TransaksiList>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.TransaksiList> call,
                                   @NonNull Response<ApiResponse.TransaksiList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allData = response.body().data;
                    adapter.setData(allData);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse.TransaksiList> call,
                                  @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        "Gagal memuat data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Search realtime ────────────────────────────────────
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    adapter.setData(allData);
                } else {
                    // Filter lokal dari data yang sudah ada
                    List<Transaksi> filtered = new ArrayList<>();
                    for (Transaksi t : allData) {
                        if (t.getJudul().toLowerCase().contains(keyword.toLowerCase())) {
                            filtered.add(t);
                        }
                    }
                    adapter.setData(filtered);
                }
            }
        });
    }

    // ─── Filter chip kategori ────────────────────────────────
    private void setupChipFilter() {
        TextView[] chips = {chipSemua, chipMakan, chipTransport, chipBelanja, chipHiburan};
        String[] kategori = {"Semua", "Makan", "Transport", "Belanja", "Hiburan"};

        for (int i = 0; i < chips.length; i++) {
            final String kat = kategori[i];
            final TextView chip = chips[i];

            chip.setOnClickListener(v -> {
                // Reset semua chip
                for (TextView c : chips) {
                    c.setBackgroundResource(R.drawable.bg_chip_normal);
                    c.setTextColor(requireContext().getColor(R.color.text_secondary));
                }
                // Aktifkan chip yang diklik
                chip.setBackgroundResource(R.drawable.bg_chip_active);
                chip.setTextColor(requireContext().getColor(android.R.color.white));

                // Filter data
                if (kat.equals("Semua")) {
                    adapter.setData(allData);
                } else {
                    List<Transaksi> filtered = new ArrayList<>();
                    for (Transaksi t : allData) {
                        if (t.getKategori().equals(kat)) filtered.add(t);
                    }
                    adapter.setData(filtered);
                }
            });
        }
    }

    // ─── Dialog konfirmasi hapus ─────────────────────────────
    private void showDeleteDialog(Transaksi transaksi) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Transaksi")
                .setMessage("Yakin ingin menghapus \"" + transaksi.getJudul() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteTransaksi(transaksi))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteTransaksi(Transaksi transaksi) {
        apiService.deleteTransaksi(transaksi.getId()).enqueue(
                new Callback<ApiResponse.GeneralResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse.GeneralResponse> call,
                                           @NonNull Response<ApiResponse.GeneralResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().success) {
                            Toast.makeText(requireContext(),
                                    "Transaksi dihapus", Toast.LENGTH_SHORT).show();
                            loadSemuaTransaksi(); // refresh list
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse.GeneralResponse> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(requireContext(),
                                "Gagal menghapus: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSemuaTransaksi();
    }
}