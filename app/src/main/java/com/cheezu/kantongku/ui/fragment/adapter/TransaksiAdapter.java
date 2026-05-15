package com.cheezu.kantongku.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cheezu.kantongku.R;
import com.cheezu.kantongku.data.api.model.Transaksi;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {

    private List<Transaksi> listTransaksi = new ArrayList<>();
    private final Context context;
    private OnItemClickListener listener;

    // ─── Interface untuk klik item ──────────────────────────
    public interface OnItemClickListener {
        void onItemClick(Transaksi transaksi);
        void onItemLongClick(Transaksi transaksi);
    }

    public TransaksiAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ─── Update data dari LiveData ───────────────────────────
    public void setData(List<Transaksi> data) {
        this.listTransaksi = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaksi item = listTransaksi.get(position);

        // Nama & kategori
        holder.tvNama.setText(item.getJudul());
        holder.tvKategori.setText(item.getKategori());

        // Format tanggal
        try {
            SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            Date date = inputSdf.parse(item.getTanggal());
            holder.tvTanggal.setText(date != null ? outputSdf.format(date) : item.getTanggal());
        } catch (Exception e) {
            holder.tvTanggal.setText(item.getTanggal());
        }

        // Format nominal ke Rupiah
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String nominalFormatted = formatter.format(item.getNominal())
                .replace("Rp", "Rp ").replace(",00", "");

        if (item.getTipe().equals("pengeluaran")) {
            holder.tvNominal.setText("- " + nominalFormatted);
            holder.tvNominal.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
        } else {
            holder.tvNominal.setText("+ " + nominalFormatted);
            holder.tvNominal.setTextColor(ContextCompat.getColor(context, R.color.income_green));
        }

        // Icon kategori
        holder.ivIcon.setImageResource(getIconByKategori(item.getKategori()));
        if (holder.ivIcon.getBackground() != null) {
            holder.ivIcon.getBackground().setTint(
                    ContextCompat.getColor(context, getBgColorByKategori(item.getKategori()))
            );
        } else {
            holder.ivIcon.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, getBgColorByKategori(item.getKategori()))
            );
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listTransaksi != null ? listTransaksi.size() : 0;
    }

    // ─── ViewHolder ─────────────────────────────────────────
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvNama, tvKategori, tvTanggal, tvNominal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon      = itemView.findViewById(R.id.iv_kategori_icon);
            tvNama      = itemView.findViewById(R.id.tv_nama_transaksi);
            tvKategori  = itemView.findViewById(R.id.tv_kategori);
            tvTanggal   = itemView.findViewById(R.id.tv_tanggal);
            tvNominal   = itemView.findViewById(R.id.tv_nominal);
        }
    }

    // ─── Helper: icon per kategori ───────────────────────────
    private int getIconByKategori(String kategori) {
        switch (kategori) {
            case "Makan":       return R.drawable.ic_food;
            case "Transport":   return R.drawable.ic_transport;
            case "Belanja":     return R.drawable.ic_shopping;
            case "Hiburan":     return R.drawable.ic_game;
            case "Kesehatan":   return R.drawable.ic_health;
            case "Pemasukan":   return R.drawable.ic_arrow_down;
            default:            return R.drawable.ic_other;
        }
    }

    // ─── Helper: warna background icon per kategori ─────────
    private int getBgColorByKategori(String kategori) {
        switch (kategori) {
            case "Makan":       return R.color.icon_bg_makan;
            case "Transport":   return R.color.icon_bg_transport;
            case "Belanja":     return R.color.icon_bg_belanja;
            case "Hiburan":     return R.color.icon_bg_hiburan;
            case "Pemasukan":   return R.color.icon_bg_pemasukan;
            default:            return R.color.icon_bg_lainnya;
        }
    }
}