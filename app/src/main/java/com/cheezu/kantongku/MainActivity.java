package com.cheezu.kantongku;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cheezu.kantongku.ui.fragment.DashboardFragment;
import com.cheezu.kantongku.ui.fragment.RiwayatFragment;
import com.cheezu.kantongku.ui.fragment.StatistikFragment;
import com.cheezu.kantongku.ui.fragment.SetelanFragment;
import com.cheezu.kantongku.ui.fragment.TambahTransaksiFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Tampilkan Dashboard saat pertama buka
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new DashboardFragment();
            } else if (id == R.id.nav_riwayat) {
                fragment = new RiwayatFragment();
            } else if (id == R.id.nav_tambah) {
                fragment = new TambahTransaksiFragment();
            } else if (id == R.id.nav_statistik) {
                fragment = new StatistikFragment();
            } else if (id == R.id.nav_setelan) {
                fragment = new SetelanFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}