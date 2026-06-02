package com.cheezu.kantongku.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cheezu.kantongku.R;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class NotificationHelper {

    private static final String CHANNEL_ID       = "budget_channel";
    private static final String CHANNEL_NAME     = "Budget Notifikasi";
    private static final String CHANNEL_ID_REMINDER   = "reminder_channel";
    private static final String CHANNEL_NAME_REMINDER = "Pengingat Harian";
    private static final int    NOTIF_ID         = 1001;
    private static final int    NOTIF_ID_REMINDER = 1002;

    // ─── Buat notification channel ───────────────────────────
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel budget
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifikasi peringatan budget");

            // Channel reminder
            NotificationChannel channelReminder = new NotificationChannel(
                    CHANNEL_ID_REMINDER, CHANNEL_NAME_REMINDER, NotificationManager.IMPORTANCE_DEFAULT);
            channelReminder.setDescription("Pengingat input transaksi harian");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                manager.createNotificationChannel(channelReminder);
            }
        }
    }

    // ─── Notifikasi peringatan budget ────────────────────────
    public static void kirimNotifBudget(Context context, String judul, String pesan) {
        simpanNotif(context, judul, pesan);
        createChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(judul)
                .setContentText(pesan)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // ─── Notifikasi reset budget bulanan ─────────────────────
    public static void showBudgetResetNotification(Context context) {
        simpanNotif(context, "🔄 Budget Direset!", "Budget bulan baru telah dimulai."); // tambah ini
        createChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🎉 Budget Bulan Baru Dimulai!")
                .setContentText("Yuk mulai catat pengeluaran bulan ini dan kelola keuangan lebih baik!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID + 10, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    // ─── Notifikasi pengingat harian ─────────────────────────
    public static void kirimNotifReminder(Context context) {
        createChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("📝 Catat transaksimu hari ini!")
                .setContentText("Jangan lupa catat pemasukan & pengeluaran hari ini ya!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_REMINDER, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    // ─── Simpan riwayat notifikasi ───────────────────────
    public static void simpanNotif(Context context, String judul, String pesan) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String existing = prefs.getString("riwayat_notif", "");
        String waktu = new java.text.SimpleDateFormat("dd MMM HH:mm",
                new java.util.Locale("id","ID")).format(new java.util.Date());
        String entry = waktu + "||" + judul + "||" + pesan;
        // Simpan max 20 notifikasi
        String[] arr = existing.isEmpty() ? new String[0] : existing.split(";;");
        StringBuilder sb = new StringBuilder(entry);
        int max = Math.min(arr.length, 19);
        for (int i = 0; i < max; i++) sb.append(";;").append(arr[i]);
        prefs.edit().putString("riwayat_notif", sb.toString()).apply();
    }

    public static java.util.List<String[]> getNotifikasi(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String existing = prefs.getString("riwayat_notif", "");
        java.util.List<String[]> list = new java.util.ArrayList<>();
        if (existing.isEmpty()) return list;
        for (String entry : existing.split(";;")) {
            String[] parts = entry.split("\\|\\|");
            if (parts.length == 3) list.add(parts);
        }
        return list;
    }

    public static void hapusSemuaNotif(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().remove("riwayat_notif").apply();
    }
}