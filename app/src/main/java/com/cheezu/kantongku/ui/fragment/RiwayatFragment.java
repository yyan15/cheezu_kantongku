package com.cheezu.kantongku.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class RiwayatFragment extends Fragment {

    private RecyclerView rvRiwayat;
    private EditText etSearch;
    private TextView chipSemua, chipMakan, chipTransport, chipBelanja, chipHiburan, chipKesehatan, chipLainnya;
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

        rvRiwayat     = view.findViewById(R.id.rv_riwayat);
        etSearch      = view.findViewById(R.id.et_search);
        chipSemua     = view.findViewById(R.id.chip_semua);
        chipMakan     = view.findViewById(R.id.chip_makan);
        chipTransport = view.findViewById(R.id.chip_transport);
        chipBelanja   = view.findViewById(R.id.chip_belanja);
        chipHiburan   = view.findViewById(R.id.chip_hiburan);
        chipKesehatan = view.findViewById(R.id.chip_kesehatan);
        chipLainnya   = view.findViewById(R.id.chip_lainnya);

        adapter = new TransaksiAdapter(requireContext());
        rvRiwayat.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRiwayat.setAdapter(adapter);

        apiService = ApiClient.getApiService();
        // Tombol filter
        ImageButton btnFilter = view.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> showFilterDialog());


            adapter.setOnItemClickListener(new TransaksiAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Transaksi transaksi) {
                    showDetailDialog(transaksi);
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

    private void loadSemuaTransaksi() {
        apiService.getAllTransaksi().enqueue(new Callback<ApiResponse.TransaksiList>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.TransaksiList> call,
                                   @NonNull Response<ApiResponse.TransaksiList> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null) {
                    allData = response.body().data;
                } else {
                    allData = new ArrayList<>();
                }
                adapter.setData(allData);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse.TransaksiList> call,
                                  @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                allData = new ArrayList<>();
                adapter.setData(allData);
                Toast.makeText(requireContext(),
                        "Gagal memuat data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_filter, null);
        dialog.setContentView(view);

        TextView btnSemua       = view.findViewById(R.id.btn_filter_semua);
        TextView btnPengeluaran = view.findViewById(R.id.btn_filter_pengeluaran);
        TextView btnPemasukan   = view.findViewById(R.id.btn_filter_pemasukan);
        TextView btnTerbaru     = view.findViewById(R.id.btn_filter_terbaru);
        TextView btnTerlama     = view.findViewById(R.id.btn_filter_terlama);
        TextView btnTutup       = view.findViewById(R.id.btn_filter_tutup);

        btnSemua.setOnClickListener(v -> {
            adapter.setData(allData);
            dialog.dismiss();
        });

        btnPengeluaran.setOnClickListener(v -> {
            List<Transaksi> filtered = new ArrayList<>();
            for (Transaksi t : allData) {
                if ("pengeluaran".equals(t.getTipe())) filtered.add(t);
            }
            adapter.setData(filtered);
            dialog.dismiss();
        });

        btnPemasukan.setOnClickListener(v -> {
            List<Transaksi> filtered = new ArrayList<>();
            for (Transaksi t : allData) {
                if ("pemasukan".equals(t.getTipe())) filtered.add(t);
            }
            adapter.setData(filtered);
            dialog.dismiss();
        });

        btnTerbaru.setOnClickListener(v -> {
            List<Transaksi> sorted = new ArrayList<>(allData);
            java.util.Collections.sort(sorted, (a, b) -> {
                if (a.getTanggal() == null || b.getTanggal() == null) return 0;
                return b.getTanggal().compareTo(a.getTanggal());
            });
            adapter.setData(sorted);
            dialog.dismiss();
        });

        btnTerlama.setOnClickListener(v -> {
            List<Transaksi> sorted = new ArrayList<>(allData);
            java.util.Collections.sort(sorted, (a, b) -> {
                if (a.getTanggal() == null || b.getTanggal() == null) return 0;
                return a.getTanggal().compareTo(b.getTanggal());
            });
            adapter.setData(sorted);
            dialog.dismiss();
        });

        btnTutup.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isAdded() || allData == null) return;
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    adapter.setData(allData);
                } else {
                    List<Transaksi> filtered = new ArrayList<>();
                    for (Transaksi t : allData) {
                        if (t.getJudul().toLowerCase().contains(keyword.toLowerCase()))
                            filtered.add(t);
                    }
                    adapter.setData(filtered);
                }
            }
        });
    }

    private void setupChipFilter() {
        TextView[] chips = {chipSemua, chipMakan, chipTransport, chipBelanja, chipHiburan, chipKesehatan, chipLainnya};
        String[] kategori = {"Semua", "Makan", "Transport", "Belanja", "Hiburan", "Kesehatan", "Lainnya"};

        for (int i = 0; i < chips.length; i++) {
            final String kat = kategori[i];
            final TextView chip = chips[i];
            chip.setOnClickListener(v -> {
                if (!isAdded() || getContext() == null) return;
                for (TextView c : chips) {
                    c.setBackgroundResource(R.drawable.bg_chip_normal);
                    c.setTextColor(requireContext().getColor(R.color.text_secondary));
                }
                chip.setBackgroundResource(R.drawable.bg_chip_active);
                chip.setTextColor(requireContext().getColor(android.R.color.white));

                if (allData == null) { adapter.setData(new ArrayList<>()); return; }
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

    private void showDeleteDialog(Transaksi transaksi) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Transaksi")
                .setMessage("Yakin ingin menghapus \"" + transaksi.getJudul() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteTransaksi(transaksi))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showDetailDialog(Transaksi transaksi) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_detail_transaksi, null);
        dialog.setContentView(view);

        // Format nominal
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String nominal = fmt.format(transaksi.getNominal())
                .replace("Rp", "Rp ").replace(",00", "");

        // Format tanggal
        String tanggal = transaksi.getTanggal();
        try {
            SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            tanggal = outputSdf.format(inputSdf.parse(transaksi.getTanggal()));
        } catch (Exception e) {
            tanggal = transaksi.getTanggal();
        }

        String catatan = (transaksi.getCatatan() != null && !transaksi.getCatatan().isEmpty())
                ? transaksi.getCatatan() : "Tidak ada catatan";

        boolean isPengeluaran = transaksi.getTipe().equals("pengeluaran");

        // Bind views
        TextView tvJudul    = view.findViewById(R.id.tv_detail_judul);
        TextView tvNominal  = view.findViewById(R.id.tv_detail_nominal);
        TextView tvTipe     = view.findViewById(R.id.tv_detail_tipe);
        TextView tvKategori = view.findViewById(R.id.tv_detail_kategori);
        TextView tvTanggal  = view.findViewById(R.id.tv_detail_tanggal);
        TextView tvCatatan  = view.findViewById(R.id.tv_detail_catatan);
        TextView btnHapus   = view.findViewById(R.id.btn_detail_hapus);
        TextView btnTutup   = view.findViewById(R.id.btn_detail_tutup);

        tvJudul.setText(transaksi.getJudul());
        tvNominal.setText((isPengeluaran ? "- " : "+ ") + nominal);
        tvNominal.setTextColor(requireContext().getColor(
                isPengeluaran ? R.color.expense_red : R.color.income_green));
        tvTipe.setText(isPengeluaran ? "🔴 Pengeluaran" : "🟢 Pemasukan");
        tvKategori.setText(transaksi.getKategori());
        tvTanggal.setText(tanggal);
        tvCatatan.setText(catatan);

        btnHapus.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteDialog(transaksi);
        });
        btnTutup.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteTransaksi(Transaksi transaksi) {
        apiService.deleteTransaksi(transaksi.getId()).enqueue(
                new Callback<ApiResponse.GeneralResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse.GeneralResponse> call,
                                           @NonNull Response<ApiResponse.GeneralResponse> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().success) {
                            Toast.makeText(requireContext(),
                                    "Transaksi dihapus", Toast.LENGTH_SHORT).show();
                            loadSemuaTransaksi();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiResponse.GeneralResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
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