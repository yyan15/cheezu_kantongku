package com.cheezu.kantongku.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cheezu.kantongku.R;

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
        createChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🔄 Budget Direset!")
                .setContentText("Budget bulan baru telah dimulai. Selamat mengelola keuangan!")
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
}