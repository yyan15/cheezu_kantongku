package com.cheezu.kantongku.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.cheezu.kantongku.data.api.model.Transaksi;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportHelper {

    public static void exportToPdf(Context context, List<Transaksi> data) {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());

        int pageWidth  = 595;
        int pageHeight = 842;
        int margin     = 40;
        int y          = 80;
        int lineHeight = 24;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // ─── Header ───────────────────────────────────────────
        paint.setColor(Color.parseColor("#0D9488"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, pageWidth, 56, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20f);
        paint.setFakeBoldText(true);
        canvas.drawText("Laporan Keuangan - CheeuzKantongku", margin, 36, paint);

        paint.setTextSize(11f);
        paint.setFakeBoldText(false);
        String tglExport = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("id","ID")).format(new Date());
        canvas.drawText("Diekspor: " + tglExport, margin, 52, paint);

        // ─── Hitung total ─────────────────────────────────────
        double totalPengeluaran = 0, totalPemasukan = 0;
        for (Transaksi t : data) {
            if ("pengeluaran".equalsIgnoreCase(t.getTipe())) totalPengeluaran += t.getNominal();
            else totalPemasukan += t.getNominal();
        }
        double saldo = totalPemasukan - totalPengeluaran;

        // ─── Summary box ──────────────────────────────────────
        y = 80;
        paint.setColor(Color.parseColor("#F0FDF4"));
        canvas.drawRect(margin, y, pageWidth - margin, y + 60, paint);

        paint.setColor(Color.parseColor("#0D9488"));
        paint.setTextSize(11f);
        paint.setFakeBoldText(true);
        canvas.drawText("Total Pemasukan", margin + 8, y + 18, paint);
        canvas.drawText("Total Pengeluaran", margin + 180, y + 18, paint);
        canvas.drawText("Saldo", margin + 380, y + 18, paint);

        paint.setTextSize(13f);
        canvas.drawText(fmt.format(totalPemasukan).replace(",00",""), margin + 8, y + 48, paint);

        paint.setColor(Color.parseColor("#EF4444"));
        canvas.drawText(fmt.format(totalPengeluaran).replace(",00",""), margin + 180, y + 48, paint);

        paint.setColor(saldo >= 0 ? Color.parseColor("#0D9488") : Color.parseColor("#EF4444"));
        canvas.drawText(fmt.format(saldo).replace(",00",""), margin + 380, y + 48, paint);

        // ─── Table header ─────────────────────────────────────
        y += 80;
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawRect(margin, y, pageWidth - margin, y + 28, paint);

        paint.setColor(Color.parseColor("#334155"));
        paint.setTextSize(11f);
        paint.setFakeBoldText(true);
        canvas.drawText("Tanggal", margin + 4, y + 18, paint);
        canvas.drawText("Judul", margin + 80, y + 18, paint);
        canvas.drawText("Kategori", margin + 260, y + 18, paint);
        canvas.drawText("Tipe", margin + 360, y + 18, paint);
        canvas.drawText("Nominal", margin + 420, y + 18, paint);

        // ─── Table rows ───────────────────────────────────────
        y += 28;
        paint.setFakeBoldText(false);
        paint.setTextSize(10f);

        int pageNum = 1;
        for (int i = 0; i < data.size(); i++) {
            Transaksi t = data.get(i);

            if (y > pageHeight - 60) {
                document.finishPage(page);
                pageNum++;
                PdfDocument.PageInfo newPageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                page = document.startPage(newPageInfo);
                canvas = page.getCanvas();
                y = 40;
            }

            if (i % 2 == 0) {
                paint.setColor(Color.parseColor("#F8FAFC"));
                canvas.drawRect(margin, y, pageWidth - margin, y + lineHeight, paint);
            }

            String tanggal = t.getTanggal();
            try {
                tanggal = sdf.format(inputSdf.parse(t.getTanggal()));
            } catch (Exception e) {
                if (tanggal != null && tanggal.length() >= 10)
                    tanggal = tanggal.substring(0, 10);
            }

            boolean isPengeluaran = "pengeluaran".equalsIgnoreCase(t.getTipe());

            paint.setColor(Color.parseColor("#334155"));
            canvas.drawText(tanggal != null ? tanggal : "-", margin + 4, y + 16, paint);

            String judul = t.getJudul() != null ? t.getJudul() : "-";
            if (judul.length() > 22) judul = judul.substring(0, 22) + "...";
            canvas.drawText(judul, margin + 80, y + 16, paint);

            canvas.drawText(t.getKategori() != null ? t.getKategori() : "-", margin + 260, y + 16, paint);

            paint.setColor(isPengeluaran ? Color.parseColor("#EF4444") : Color.parseColor("#0D9488"));
            canvas.drawText(isPengeluaran ? "Keluar" : "Masuk", margin + 360, y + 16, paint);
            canvas.drawText(fmt.format(t.getNominal()).replace(",00", ""), margin + 420, y + 16, paint);

            paint.setColor(Color.parseColor("#E2E8F0"));
            paint.setStrokeWidth(0.5f);
            canvas.drawLine(margin, y + lineHeight, pageWidth - margin, y + lineHeight, paint);

            y += lineHeight;
        }

        document.finishPage(page);

        // ─── Simpan file ──────────────────────────────────────
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "CheeuzKantongku");
            if (!dir.exists()) dir.mkdirs();

            String fileName = "Laporan_" + new SimpleDateFormat("yyyyMMdd_HHmm",
                    Locale.getDefault()).format(new Date()) + ".pdf";
            File file = new File(dir, fileName);

            document.writeTo(new FileOutputStream(file));
            document.close();

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file);

            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                    context, 0,
                    new android.content.Intent(android.content.Intent.ACTION_VIEW)
                            .setDataAndType(uri, "application/pdf")
                            .addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

            android.app.NotificationManager manager =
                    (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                android.app.NotificationChannel channel = new android.app.NotificationChannel(
                        "export_channel", "Export Laporan",
                        android.app.NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }

            androidx.core.app.NotificationCompat.Builder builder =
                    new androidx.core.app.NotificationCompat.Builder(context, "export_channel")
                            .setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setContentTitle("Laporan siap! 📄")
                            .setContentText("Ketuk untuk membuka " + fileName)
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent);

            try {
                manager.notify(2001, builder.build());
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            Toast.makeText(context, "PDF tersimpan di Downloads!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            document.close();
            Toast.makeText(context, "Gagal export: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}