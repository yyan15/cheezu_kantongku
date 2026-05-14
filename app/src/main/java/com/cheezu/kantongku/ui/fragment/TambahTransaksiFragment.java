package com.cheezu.kantongku.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahTransaksiFragment extends Fragment {

    private Button btnPengeluaran, btnPemasukan, btnSimpan;
    private EditText etNominal, etCatatan;
    private TextView tvTanggal;
    private LinearLayout btnPilihTanggal;
    private LinearLayout catMakan, catTransport, catBelanja,
            catHiburan, catKesehatan, catLainnya;

    private String tipeSelected    = "pengeluaran";
    private String kategoriSelected = "Makan";
    private String tanggalSelected;

    private TransaksiApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tambah_transaksi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init views
        btnPengeluaran  = view.findViewById(R.id.btn_pengeluaran);
        btnPemasukan    = view.findViewById(R.id.btn_pemasukan);
        btnSimpan       = view.findViewById(R.id.btn_simpan);
        etNominal       = view.findViewById(R.id.et_nominal);
        etCatatan       = view.findViewById(R.id.et_catatan);
        tvTanggal       = view.findViewById(R.id.tv_tanggal);
        btnPilihTanggal = view.findViewById(R.id.btn_pilih_tanggal);

        catMakan        = view.findViewById(R.id.cat_makan);
        catTransport    = view.findViewById(R.id.cat_transport);
        catBelanja      = view.findViewById(R.id.cat_belanja);
        catHiburan      = view.findViewById(R.id.cat_hiburan);
        catKesehatan    = view.findViewById(R.id.cat_kesehatan);
        catLainnya      = view.findViewById(R.id.cat_lainnya);

        apiService = ApiClient.getApiService();

        // Set tanggal default = hari ini
        Calendar cal = Calendar.getInstance();
        tanggalSelected = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
        tvTanggal.setText(new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID")).format(cal.getTime()));

        setupToggleTipe();
        setupKategori();
        setupDatePicker();
        setupTombolSimpan();
    }

    // ─── Toggle Pengeluaran / Pemasukan ─────────────────────
    private void setupToggleTipe() {
        btnPengeluaran.setOnClickListener(v -> {
            tipeSelected = "pengeluaran";
            btnPengeluaran.setBackgroundTintList(
                    requireContext().getColorStateList(R.color.teal_primary));
            btnPengeluaran.setTextColor(requireContext().getColor(android.R.color.white));
            btnPemasukan.setBackgroundTintList(
                    requireContext().getColorStateList(R.color.surface_secondary));
            btnPemasukan.setTextColor(requireContext().getColor(R.color.text_secondary));
        });

        btnPemasukan.setOnClickListener(v -> {
            tipeSelected = "pemasukan";
            btnPemasukan.setBackgroundTintList(
                    requireContext().getColorStateList(R.color.teal_primary));
            btnPemasukan.setTextColor(requireContext().getColor(android.R.color.white));
            btnPengeluaran.setBackgroundTintList(
                    requireContext().getColorStateList(R.color.surface_secondary));
            btnPengeluaran.setTextColor(requireContext().getColor(R.color.text_secondary));
        });
    }

    // ─── Pilih Kategori ─────────────────────────────────────
    private void setupKategori() {
        LinearLayout[] semuaKategori = {catMakan, catTransport, catBelanja,
                catHiburan, catKesehatan, catLainnya};
        String[] namaKategori = {"Makan", "Transport", "Belanja",
                "Hiburan", "Kesehatan", "Lainnya"};

        for (int i = 0; i < semuaKategori.length; i++) {
            final String nama = namaKategori[i];
            final LinearLayout item = semuaKategori[i];

            item.setOnClickListener(v -> {
                kategoriSelected = nama;
                // Reset semua ke normal
                for (LinearLayout cat : semuaKategori) {
                    cat.setBackgroundResource(R.drawable.bg_category_normal);
                    // Reset warna teks anak TextView
                    if (cat.getChildCount() > 1 && cat.getChildAt(1) instanceof TextView) {
                        ((TextView) cat.getChildAt(1)).setTextColor(
                                requireContext().getColor(R.color.text_primary));
                    }
                }
                // Set yang dipilih jadi aktif
                item.setBackgroundResource(R.drawable.bg_category_selected);
                if (item.getChildCount() > 1 && item.getChildAt(1) instanceof TextView) {
                    ((TextView) item.getChildAt(1)).setTextColor(
                            requireContext().getColor(R.color.teal_dark));
                }
            });
        }
    }

    // ─── Date Picker ────────────────────────────────────────
    private void setupDatePicker() {
        btnPilihTanggal.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (datePicker, year, month, day) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, day);

                        // Format untuk dikirim ke Laravel (yyyy-MM-dd)
                        tanggalSelected = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(selected.getTime());

                        // Format untuk ditampilkan ke user
                        tvTanggal.setText(new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"))
                                .format(selected.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    // ─── Simpan Transaksi ke Laravel ────────────────────────
    private void setupTombolSimpan() {
        btnSimpan.setOnClickListener(v -> {

            String nominalStr = etNominal.getText().toString().trim();
            String catatan    = etCatatan.getText().toString().trim();

            // Validasi nominal
            if (nominalStr.isEmpty()) {
                etNominal.setError("Nominal tidak boleh kosong");
                etNominal.requestFocus();
                return;
            }

            double nominal = Double.parseDouble(nominalStr);
            if (nominal <= 0) {
                etNominal.setError("Nominal harus lebih dari 0");
                return;
            }

            // Buat objek Transaksi
            // Judul otomatis dari kategori jika tidak diisi
            String judul = kategoriSelected + " - " +
                    new SimpleDateFormat("dd MMM", new Locale("id", "ID"))
                            .format(Calendar.getInstance().getTime());

            Transaksi transaksi = new Transaksi(
                    judul, kategoriSelected, tipeSelected,
                    nominal, catatan, tanggalSelected
            );

            // Disable tombol supaya tidak double submit
            btnSimpan.setEnabled(false);
            btnSimpan.setText("Menyimpan...");

            // POST ke Laravel
            apiService.tambahTransaksi(transaksi).enqueue(new Callback<ApiResponse.TransaksiSingle>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse.TransaksiSingle> call,
                                       @NonNull Response<ApiResponse.TransaksiSingle> response) {
                    btnSimpan.setEnabled(true);
                    btnSimpan.setText("Simpan Transaksi");

                    if (response.isSuccessful() && response.body() != null
                            && response.body().success) {
                        Toast.makeText(requireContext(),
                                "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show();
                        resetForm();
                    } else {
                        Toast.makeText(requireContext(),
                                "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse.TransaksiSingle> call,
                                      @NonNull Throwable t) {
                    btnSimpan.setEnabled(true);
                    btnSimpan.setText("Simpan Transaksi");
                    Toast.makeText(requireContext(),
                            "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ─── Reset form setelah simpan ───────────────────────────
    private void resetForm() {
        etNominal.setText("");
        etCatatan.setText("");
        tipeSelected     = "pengeluaran";
        kategoriSelected = "Makan";

        btnPengeluaran.setBackgroundTintList(
                requireContext().getColorStateList(R.color.teal_primary));
        btnPengeluaran.setTextColor(requireContext().getColor(android.R.color.white));
        btnPemasukan.setBackgroundTintList(
                requireContext().getColorStateList(R.color.surface_secondary));
        btnPemasukan.setTextColor(requireContext().getColor(R.color.text_secondary));
    }
}