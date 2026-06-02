package com.cheezu.kantongku.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BudgetResetWorker extends Worker {

    public BudgetResetWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putLong("last_budget_reset", System.currentTimeMillis()).apply();

        NotificationHelper.showBudgetResetNotification(getApplicationContext());
        return Result.success();
    }
}