package com.cheezu.kantongku.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Locale;

public class RupiahTextWatcher implements TextWatcher {

    private final EditText editText;
    private boolean isEditing = false;

    public RupiahTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (isEditing) return;
        isEditing = true;

        // Hapus semua karakter selain angka
        String original = s.toString().replaceAll("[^\\d]", "");

        if (!original.isEmpty()) {
            try {
                long number = Long.parseLong(original);
                // Format dengan pemisah ribuan
                NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("id", "ID"));
                String formatted = formatter.format(number);
                editText.setText(formatted);
                editText.setSelection(formatted.length());
            } catch (NumberFormatException e) {
                // Abaikan jika error parsing
            }
        }

        isEditing = false;
    }

    // ─── Helper: ambil nilai angka dari EditText ─────────────
    public static double getNilai(EditText editText) {
        String text = editText.getText().toString().replaceAll("[^\\d]", "");
        if (text.isEmpty()) return 0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}