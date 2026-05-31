package com.cheezu.kantongku.ui.fragment;

import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
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
import java.util.Locale;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.cheezu.kantongku.util.ReminderWorker;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import com.cheezu.kantongku.data.api.ApiClient;
import com.cheezu.kantongku.data.api.ApiResponse;
import com.cheezu.kantongku.data.api.TransaksiApiService;
import com.cheezu.kantongku.data.api.model.Transaksi;
import com.cheezu.kantongku.util.RupiahTextWatcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetelanFragment extends Fragment {

    // SharedPreferences keys
    public static final String KEY_BUDGET_TOTAL    = "budget_total";
    public static final String KEY_NOTIF_BUDGET    = "notif_budget";
    public static final String KEY_THRESHOLD       = "threshold_persen";
    public static final String KEY_REMINDER        = "reminder_aktif";
    public static final String KEY_DARK_MODE       = "dark_mode";

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
        double budget    = prefs.getFloat(KEY_BUDGET_TOTAL, 3900000f);
        boolean notif    = prefs.getBoolean(KEY_NOTIF_BUDGET, true);
        int threshold    = prefs.getInt(KEY_THRESHOLD, 80);
        boolean reminder = prefs.getBoolean(KEY_REMINDER, false);
        boolean darkMode = prefs.getBoolean(KEY_DARK_MODE, false);

        tvBudgetTotalValue.setText(
                fmt.format(budget).replace("Rp", "Rp ").replace(",00", "") + " / bulan");
        tvThresholdValue.setText(threshold + "% dari limit");
        switchNotifBudget.setChecked(notif);
        switchReminder.setChecked(reminder);
        switchDarkMode.setChecked(darkMode);
    }

    // ─── Setup semua listener ────────────────────────────────
    private void setupListeners() {

        // Edit budget total
        itemBudgetTotal.setOnClickListener(v -> showDialogBudget());


        // Edit threshold peringatan
        itemThreshold.setOnClickListener(v -> showDialogThreshold());

        // Toggle notifikasi budget
        switchNotifBudget.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIF_BUDGET, isChecked).apply();
            Toast.makeText(requireContext(),
                    isChecked ? "Notifikasi budget aktif" : "Notifikasi budget nonaktif",
                    Toast.LENGTH_SHORT).show();
        });

        // Toggle reminder harian
        switchReminder.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_REMINDER, isChecked).apply();
            if (isChecked) {
                jadwalkanReminder();
            } else {
                batalkanReminder();
            }
        });

        // Toggle dark mode
        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Export (placeholder)
        itemExport.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Fitur export segera hadir!", Toast.LENGTH_SHORT).show()
        );

        // Reset data
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

    // ─── Dialog set budget total ─────────────────────────────
    private void showDialogBudget() {
        EditText etBudget = new EditText(requireContext());
        etBudget.setHint("Masukkan nominal budget");
        etBudget.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etBudget.setPadding(48, 32, 48, 32);
        etBudget.addTextChangedListener(new RupiahTextWatcher(etBudget));


        double currentBudget = prefs.getFloat(KEY_BUDGET_TOTAL, 3900000f);
        etBudget.setText(String.valueOf((int) currentBudget));

        new AlertDialog.Builder(requireContext())
                .setTitle("Set Budget Limit")
                .setView(etBudget)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String input = etBudget.getText().toString().trim();
                    if (!input.isEmpty()) {
                        float budget = (float) RupiahTextWatcher.getNilai(etBudget);
                        prefs.edit().putFloat(KEY_BUDGET_TOTAL, budget).apply();
                        tvBudgetTotalValue.setText(
                                fmt.format(budget).replace("Rp", "Rp ").replace(",00", "") + " / bulan");
                        Toast.makeText(requireContext(),
                                "Budget limit disimpan!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ─── Dialog set threshold peringatan ────────────────────
    private void showDialogThreshold() {
        String[] options = {"60%", "70%", "80%", "90%"};
        int[] values     = {60, 70, 80, 90};
        int current      = prefs.getInt(KEY_THRESHOLD, 80);
        int checkedItem  = 2; // default 80%
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) { checkedItem = i; break; }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Batas Peringatan Budget")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    prefs.edit().putInt(KEY_THRESHOLD, values[which]).apply();
                    tvThresholdValue.setText(values[which] + "% dari limit");
                    dialog.dismiss();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
    private void deleteSemuaData() {
        apiService.getAllTransaksi().enqueue(new Callback<ApiResponse.TransaksiList>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse.TransaksiList> call,
                                   @NonNull Response<ApiResponse.TransaksiList> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null) {
                    for (Transaksi t : response.body().data) {
                        apiService.deleteTransaksi(t.getId()).enqueue(
                                new Callback<ApiResponse.GeneralResponse>() {
                                    @Override
                                    public void onResponse(@NonNull Call<ApiResponse.GeneralResponse> call,
                                                           @NonNull Response<ApiResponse.GeneralResponse> response) {}
                                    @Override
                                    public void onFailure(@NonNull Call<ApiResponse.GeneralResponse> call,
                                                          @NonNull Throwable t) {}
                                });
                    }
                    Toast.makeText(requireContext(),
                            "Semua data berhasil dihapus!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse.TransaksiList> call,
                                  @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(),
                        "Gagal menghapus data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Dialog konfirmasi reset data ────────────────────────
    private void showDialogResetData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Semua Data")
                .setMessage("Seluruh riwayat transaksi akan dihapus permanen. Yakin?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    deleteSemuaData();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
    private void jadwalkanReminder() {
        // Hitung delay sampai jam 21:00 malam ini
        Calendar sekarang = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 21);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        // Kalau sudah lewat jam 21, jadwalkan besok
        if (sekarang.after(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        long delay = target.getTimeInMillis() - sekarang.getTimeInMillis();

        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderWork
        );

        Toast.makeText(requireContext(), "Pengingat aktif setiap jam 21:00", Toast.LENGTH_SHORT).show();
    }

    private void batalkanReminder() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("daily_reminder");
        Toast.makeText(requireContext(), "Pengingat dinonaktifkan", Toast.LENGTH_SHORT).show();
    }
}