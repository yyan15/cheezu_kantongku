package com.cheezu.kantongku.ui.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import androidx.fragment.app.Fragment;

import com.cheezu.kantongku.R;

import java.text.NumberFormat;
import java.util.Locale;

public class SetelanFragment extends Fragment {

    // SharedPreferences keys
    public static final String KEY_BUDGET_TOTAL    = "budget_total";
    public static final String KEY_NOTIF_BUDGET    = "notif_budget";
    public static final String KEY_THRESHOLD       = "threshold_persen";
    public static final String KEY_REMINDER        = "reminder_aktif";
    public static final String KEY_DARK_MODE       = "dark_mode";

    private TextView tvBudgetTotalValue, tvThresholdValue;
    private Switch switchResetBudget, switchNotifBudget, switchReminder, switchDarkMode;
    private LinearLayout itemBudgetTotal, itemBudgetKategori,
            itemThreshold, itemExport, itemResetData;

    private SharedPreferences prefs;
    private NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

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
        switchResetBudget   = view.findViewById(R.id.switch_reset_budget);
        switchNotifBudget   = view.findViewById(R.id.switch_notif_budget);
        switchReminder      = view.findViewById(R.id.switch_reminder);
        switchDarkMode      = view.findViewById(R.id.switch_dark_mode);
        itemBudgetTotal     = view.findViewById(R.id.item_budget_total);
        itemBudgetKategori  = view.findViewById(R.id.item_budget_kategori);
        itemThreshold       = view.findViewById(R.id.item_threshold);
        itemExport          = view.findViewById(R.id.item_export);
        itemResetData       = view.findViewById(R.id.item_reset_data);

        loadSavedSettings();
        setupListeners();
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
            Toast.makeText(requireContext(),
                    isChecked ? "Pengingat harian aktif" : "Pengingat harian nonaktif",
                    Toast.LENGTH_SHORT).show();
        });

        // Toggle dark mode
        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            Toast.makeText(requireContext(),
                    "Restart app untuk menerapkan tema", Toast.LENGTH_SHORT).show();
        });

        // Export (placeholder)
        itemExport.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Fitur export segera hadir!", Toast.LENGTH_SHORT).show()
        );

        // Reset data
        itemResetData.setOnClickListener(v -> showDialogResetData());
    }

    // ─── Dialog set budget total ─────────────────────────────
    private void showDialogBudget() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(android.R.layout.simple_list_item_1, null);

        EditText etBudget = new EditText(requireContext());
        etBudget.setHint("Masukkan nominal budget");
        etBudget.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etBudget.setPadding(48, 32, 48, 32);

        double currentBudget = prefs.getFloat(KEY_BUDGET_TOTAL, 3900000f);
        etBudget.setText(String.valueOf((int) currentBudget));

        new AlertDialog.Builder(requireContext())
                .setTitle("Set Budget Limit")
                .setView(etBudget)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String input = etBudget.getText().toString().trim();
                    if (!input.isEmpty()) {
                        float budget = Float.parseFloat(input);
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

    // ─── Dialog konfirmasi reset data ────────────────────────
    private void showDialogResetData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Semua Data")
                .setMessage("Seluruh riwayat transaksi akan dihapus permanen. Yakin?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    // TODO: panggil API delete all jika tersedia
                    // Untuk sekarang hanya tampilkan pesan
                    Toast.makeText(requireContext(),
                            "Data berhasil direset", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}