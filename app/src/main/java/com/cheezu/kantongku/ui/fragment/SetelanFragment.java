package com.cheezu.kantongku.ui.fragment;

import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.cheezu.kantongku.ui.activity.LoginActivity;
import android.content.Context;
import android.content.Intent;
import com.cheezu.kantongku.R;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.cheezu.kantongku.util.ReminderWorker;
import com.cheezu.kantongku.data.api.ApiClient;
import com.cheezu.kantongku.data.api.ApiResponse;
import com.cheezu.kantongku.data.api.TransaksiApiService;
import com.cheezu.kantongku.data.api.model.Transaksi;
import com.cheezu.kantongku.util.ExportHelper;
import com.cheezu.kantongku.util.RupiahTextWatcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SetelanFragment extends Fragment {

    // SharedPreferences keys
    public static final String KEY_BUDGET_TOTAL   = "budget_total";
    public static final String KEY_NOTIF_BUDGET   = "notif_budget";
    public static final String KEY_THRESHOLD      = "threshold_persen";
    public static final String KEY_REMINDER       = "reminder_aktif";
    public static final String KEY_DARK_MODE      = "dark_mode";
    public static final String KEY_RESET_BUDGET   = "reset_budget_otomatis";

    // Budget per kategori keys
    public static final String KEY_BUDGET_MAKAN     = "budget_makan";
    public static final String KEY_BUDGET_TRANSPORT = "budget_transport";
    public static final String KEY_BUDGET_BELANJA   = "budget_belanja";
    public static final String KEY_BUDGET_HIBURAN   = "budget_hiburan";
    public static final String KEY_BUDGET_KESEHATAN = "budget_kesehatan";
    public static final String KEY_BUDGET_LAINNYA   = "budget_lainnya";

    private static final String[] KATEGORI_LABELS = {
            "Makan", "Transport", "Belanja", "Hiburan", "Kesehatan", "Lainnya"
    };
    private static final String[] KATEGORI_KEYS = {
            KEY_BUDGET_MAKAN, KEY_BUDGET_TRANSPORT, KEY_BUDGET_BELANJA,
            KEY_BUDGET_HIBURAN, KEY_BUDGET_KESEHATAN, KEY_BUDGET_LAINNYA
    };
    private static final float[] KATEGORI_DEFAULT = {
            800000f, 400000f, 600000f, 300000f, 300000f, 200000f
    };

    private TextView tvBudgetTotalValue, tvThresholdValue, tvUserName, tvUserEmail;
    private Switch switchResetBudget, switchNotifBudget, switchReminder, switchDarkMode;
    private LinearLayout itemBudgetTotal, itemBudgetKategori,
            itemThreshold, itemExport, itemResetData, itemLogout;

    private SharedPreferences prefs;
    private NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private TransaksiApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setelan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        tvBudgetTotalValue  = view.findViewById(R.id.tv_budget_total_value);
        tvThresholdValue    = view.findViewById(R.id.tv_threshold_value);
        tvUserName          = view.findViewById(R.id.tv_user_name);
        tvUserEmail         = view.findViewById(R.id.tv_user_email);
        switchResetBudget   = view.findViewById(R.id.switch_reset_budget);
        switchNotifBudget   = view.findViewById(R.id.switch_notif_budget);
        switchReminder      = view.findViewById(R.id.switch_reminder);
        switchDarkMode      = view.findViewById(R.id.switch_dark_mode);
        itemBudgetTotal     = view.findViewById(R.id.item_budget_total);
        itemBudgetKategori  = view.findViewById(R.id.item_budget_kategori);
        itemThreshold       = view.findViewById(R.id.item_threshold);
        itemExport          = view.findViewById(R.id.item_export_data);
        itemResetData       = view.findViewById(R.id.item_reset_data);
        itemLogout          = view.findViewById(R.id.item_logout);
        apiService = ApiClient.getApiService();

        loadSavedSettings();
        loadUserInfo();
        setupListeners();
    }

    // ─── Load data user ──────────────────────────────────────
    private void loadUserInfo() {
        SharedPreferences kantongkuPrefs = requireContext().getSharedPreferences("KantongkuPrefs", Context.MODE_PRIVATE);
        String name = kantongkuPrefs.getString("user_name", "Pengguna");
        String email = kantongkuPrefs.getString("user_email", "Belum Login");

        tvUserName.setText(name);
        tvUserEmail.setText(email);
    }

    // ─── Load setting tersimpan ──────────────────────────────
    private void loadSavedSettings() {
        double budget        = prefs.getFloat(KEY_BUDGET_TOTAL, 3900000f);
        boolean notif        = prefs.getBoolean(KEY_NOTIF_BUDGET, true);
        int threshold        = prefs.getInt(KEY_THRESHOLD, 80);
        boolean reminder     = prefs.getBoolean(KEY_REMINDER, false);
        boolean darkMode     = prefs.getBoolean(KEY_DARK_MODE, false);
        boolean resetOtomatis = prefs.getBoolean(KEY_RESET_BUDGET, false);

        tvBudgetTotalValue.setText(
                fmt.format(budget).replace("Rp", "Rp ").replace(",00", "") + " / bulan");
        tvThresholdValue.setText(threshold + "% dari limit");
        switchNotifBudget.setChecked(notif);
        switchReminder.setChecked(reminder);
        switchDarkMode.setChecked(darkMode);
        switchResetBudget.setChecked(resetOtomatis);
    }

    private void setupListeners() {
        itemBudgetTotal.setOnClickListener(v -> showDialogBudget());

        // FITUR BARU: budget per kategori
        itemBudgetKategori.setOnClickListener(v -> showDialogBudgetKategori());

        itemThreshold.setOnClickListener(v -> showDialogThreshold());

        switchNotifBudget.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIF_BUDGET, isChecked).apply();
            Toast.makeText(requireContext(),
                    isChecked ? "Notifikasi budget aktif" : "Notifikasi budget nonaktif",
                    Toast.LENGTH_SHORT).show();
        });

        // FITUR BARU: reset budget otomatis
        switchResetBudget.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_RESET_BUDGET, isChecked).apply();
            if (isChecked) jadwalkanResetBudgetBulanan();
            else batalkanResetBudgetBulanan();
        });

        switchReminder.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_REMINDER, isChecked).apply();
            if (isChecked) jadwalkanReminder();
            else batalkanReminder();
        });

        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });

        itemExport.setOnClickListener(v -> {
            apiService.getAllTransaksi().enqueue(new Callback<ApiResponse.TransaksiList>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse.TransaksiList> call,
                                       @NonNull Response<ApiResponse.TransaksiList> response) {
                    if (!isAdded() || getContext() == null) return;
                    if (response.isSuccessful() && response.body() != null
                            && response.body().data != null) {
                        ExportHelper.exportToPdf(requireContext(), response.body().data);
                    } else {
                        Toast.makeText(requireContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ApiResponse.TransaksiList> call, @NonNull Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(requireContext(), "Gagal memuat data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        itemResetData.setOnClickListener(v -> showDialogResetData());

        // Logout
        itemLogout.setOnClickListener(v -> showDialogLogout());
    }

    private void showDialogLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // 1. Hapus token dari SharedPreferences
                    SharedPreferences kantongkuPrefs = requireContext().getSharedPreferences("KantongkuPrefs", Context.MODE_PRIVATE);
                    kantongkuPrefs.edit().clear().apply();

                    // 2. Clear Google Sign In (Optional but recommended)
                    // You might need to inject GoogleSignInClient here or just clear the local state

                    Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show();

                    // 3. Pindah ke LoginActivity
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ─── Dialog budget total ─────────────────────────────────
    private void showDialogBudget() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_input, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tv_bs_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_bs_subtitle);
        EditText etInput = view.findViewById(R.id.et_bs_input);
        TextView btnBatal = view.findViewById(R.id.btn_bs_batal);
        TextView btnSimpan = view.findViewById(R.id.btn_bs_simpan);

        tvTitle.setText("Budget Limit Total");
        tvSubtitle.setText("Atur total budget pengeluaran per bulan");
        etInput.setHint("Masukkan nominal");
        etInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etInput.addTextChangedListener(new RupiahTextWatcher(etInput));
        etInput.setText(String.valueOf((int) prefs.getFloat(KEY_BUDGET_TOTAL, 3900000f)));

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        btnSimpan.setOnClickListener(v -> {
            String input = etInput.getText().toString().trim();
            if (!input.isEmpty()) {
                float budget = (float) RupiahTextWatcher.getNilai(etInput);
                prefs.edit().putFloat(KEY_BUDGET_TOTAL, budget).apply();
                tvBudgetTotalValue.setText(
                        fmt.format(budget).replace("Rp", "Rp ").replace(",00", "") + " / bulan");
                Toast.makeText(requireContext(), "Budget limit disimpan!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    // ─── Dialog budget per kategori (FITUR BARU) ─────────────
    private void showDialogBudgetKategori() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_kategori, null);
        dialog.setContentView(view);

        String[] icons = {"🍽️", "🚌", "🛒", "🎮", "❤️", "📦"};

        LinearLayout container = view.findViewById(R.id.ll_kategori_container);
        TextView btnTutup = view.findViewById(R.id.btn_bs_tutup);

        for (int i = 0; i < KATEGORI_LABELS.length; i++) {
            final int index = i;
            float budget = prefs.getFloat(KATEGORI_KEYS[i], KATEGORI_DEFAULT[i]);

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_kategori_budget, container, false);

            TextView tvIcon  = item.findViewById(R.id.tv_icon);
            TextView tvNama  = item.findViewById(R.id.tv_nama);
            TextView tvBudget = item.findViewById(R.id.tv_budget);

            tvIcon.setText(icons[i]);
            tvNama.setText(KATEGORI_LABELS[i]);
            tvBudget.setText(fmt.format(budget).replace("Rp", "Rp ").replace(",00", ""));

            item.setOnClickListener(v -> {
                dialog.dismiss();
                showDialogSetBudgetKategori(index);
            });

            container.addView(item);
        }

        btnTutup.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDialogSetBudgetKategori(int index) {
        String nama    = KATEGORI_LABELS[index];
        String key     = KATEGORI_KEYS[index];
        float current  = prefs.getFloat(key, KATEGORI_DEFAULT[index]);

        EditText etBudget = new EditText(requireContext());
        etBudget.setHint("Budget untuk " + nama);
        etBudget.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etBudget.setPadding(48, 32, 48, 32);
        etBudget.addTextChangedListener(new RupiahTextWatcher(etBudget));
        etBudget.setText(String.valueOf((int) current));

        new AlertDialog.Builder(requireContext())
                .setTitle("Budget " + nama)
                .setMessage("Saat ini: " + fmt.format(current).replace("Rp", "Rp ").replace(",00", ""))
                .setView(etBudget)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String input = etBudget.getText().toString().trim();
                    if (!input.isEmpty()) {
                        float budget = (float) RupiahTextWatcher.getNilai(etBudget);
                        prefs.edit().putFloat(key, budget).apply();
                        Toast.makeText(requireContext(),
                                "Budget " + nama + " disimpan: " +
                                        fmt.format(budget).replace("Rp", "Rp ").replace(",00", ""),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ─── Reset budget otomatis (FITUR BARU) ──────────────────
    private void jadwalkanResetBudgetBulanan() {
        Calendar sekarang = Calendar.getInstance();
        Calendar target   = Calendar.getInstance();
        target.set(Calendar.DAY_OF_MONTH, 1);
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 1);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        target.add(Calendar.MONTH, 1); // tanggal 1 bulan depan

        long delayMs = target.getTimeInMillis() - sekarang.getTimeInMillis();

        PeriodicWorkRequest resetWork = new PeriodicWorkRequest.Builder(
                com.cheezu.kantongku.util.BudgetResetWorker.class, 30, TimeUnit.DAYS)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "budget_reset_bulanan",
                ExistingPeriodicWorkPolicy.REPLACE,
                resetWork);

        Toast.makeText(requireContext(),
                "Reset budget otomatis aktif setiap awal bulan", Toast.LENGTH_SHORT).show();
    }

    private void batalkanResetBudgetBulanan() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("budget_reset_bulanan");
        Toast.makeText(requireContext(), "Reset budget otomatis dinonaktifkan", Toast.LENGTH_SHORT).show();
    }

    // ─── Dialog threshold ────────────────────────────────────
    private void showDialogThreshold() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_threshold, null);
        dialog.setContentView(view);

        int[] values = {60, 70, 80, 90};
        int[] ids = {R.id.btn_60, R.id.btn_70, R.id.btn_80, R.id.btn_90};
        int current = prefs.getInt(KEY_THRESHOLD, 80);

        TextView btnTutup = view.findViewById(R.id.btn_bs_tutup);

        for (int i = 0; i < ids.length; i++) {
            final int val = values[i];
            TextView btn = view.findViewById(ids[i]);
            if (val == current) {
                btn.setBackgroundResource(R.drawable.bg_chip_active);
                btn.setTextColor(Color.WHITE);
            }
            btn.setOnClickListener(v -> {
                prefs.edit().putInt(KEY_THRESHOLD, val).apply();
                tvThresholdValue.setText(val + "% dari limit");
                Toast.makeText(requireContext(), "Batas peringatan: " + val + "%", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        btnTutup.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ─── Reset data ──────────────────────────────────────────
    private void showDialogResetData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Semua Data")
                .setMessage("Seluruh riwayat transaksi akan dihapus permanen. Yakin?")
                .setPositiveButton("Reset", (dialog, which) -> deleteSemuaData())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteSemuaData() {
        apiService.getAllTransaksi().enqueue(new Callback<ApiResponse.TransaksiList>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.TransaksiList> call,
                                   @NonNull Response<ApiResponse.TransaksiList> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (Transaksi t : response.body().data) {
                        apiService.deleteTransaksi(t.getId()).enqueue(new Callback<ApiResponse.GeneralResponse>() {
                            @Override public void onResponse(@NonNull Call<ApiResponse.GeneralResponse> call, @NonNull Response<ApiResponse.GeneralResponse> response) {}
                            @Override public void onFailure(@NonNull Call<ApiResponse.GeneralResponse> call, @NonNull Throwable t) {}
                        });
                    }
                    Toast.makeText(requireContext(), "Semua data berhasil dihapus!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse.TransaksiList> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(), "Gagal menghapus data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Reminder harian ─────────────────────────────────────
    private void jadwalkanReminder() {
        Calendar sekarang = Calendar.getInstance();
        Calendar target   = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 21);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        if (sekarang.after(target)) target.add(Calendar.DAY_OF_MONTH, 1);

        long delay = target.getTimeInMillis() - sekarang.getTimeInMillis();

        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                com.cheezu.kantongku.util.ReminderWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "daily_reminder", ExistingPeriodicWorkPolicy.REPLACE, reminderWork);

        Toast.makeText(requireContext(), "Pengingat aktif setiap jam 21:00", Toast.LENGTH_SHORT).show();
    }

    private void batalkanReminder() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("daily_reminder");
        Toast.makeText(requireContext(), "Pengingat dinonaktifkan", Toast.LENGTH_SHORT).show();
    }
}