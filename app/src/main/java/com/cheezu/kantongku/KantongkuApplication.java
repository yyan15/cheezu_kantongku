package com.cheezu.kantongku;

import android.app.Application;
import com.cheezu.kantongku.data.api.ApiClient;

public class KantongkuApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Inisialisasi ApiClient sekali saja saat aplikasi dimulai
        ApiClient.init(this);
    }
}
