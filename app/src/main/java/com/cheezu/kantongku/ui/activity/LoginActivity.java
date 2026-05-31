package com.cheezu.kantongku.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cheezu.kantongku.R;
import com.cheezu.kantongku.MainActivity;
import com.cheezu.kantongku.data.api.ApiClient;
import com.cheezu.kantongku.data.api.model.LoginResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "GoogleSignIn";
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 0. Check Auto-Login
        SharedPreferences sharedPref = getSharedPreferences("KantongkuPrefs", MODE_PRIVATE);
        String tokenTersimpan = sharedPref.getString("token_akses", null);

        if (tokenTersimpan != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        Button btnGoogleLogin = findViewById(R.id.btn_google_sign_in);

        // 1. Konfigurasi Google Sign-In
        String webClientId = getString(R.string.web_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 2. DAFTARKAN LAUNCHER UNTUK MENANGKAP HASIL LOGIN
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Selalu coba proses data untuk mendapatkan ApiException yang lebih spesifik
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }
        );

        // 3. Trigger saat tombol login diklik
        btnGoogleLogin.setOnClickListener(v -> {
            // Sign out dulu untuk mereset status login (mencegah error 12502)
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
    }

    // 4. DI SINI TEMPAT UNTUK MENGAMBIL idToken NYA!
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // ==========================================
            // KODE UTAMA YANG KAMU TANYAKAN ADA DI SINI:
            // ==========================================
            String idToken = account.getIdToken();

            if (idToken != null) {
                Log.d(TAG, "ID Token Google: " + idToken.substring(0, Math.min(idToken.length(), 20)) + "...");
            }

            // Langkah selanjutnya: Kirim token ini ke API Laravel kamu
            sendTokenToLaravelServer(idToken);

        } catch (ApiException e) {
            // Cetak detail error untuk debugging
            Log.e(TAG, "Google Sign-In Gagal. Kode Status: " + e.getStatusCode());
            Log.e(TAG, "Pesan Error: " + com.google.android.gms.common.api.CommonStatusCodes.getStatusCodeString(e.getStatusCode()));
            
            String pesan = "Google Sign-In Gagal (" + e.getStatusCode() + ")";
            if (e.getStatusCode() == 10) pesan = "Developer Error: Cek SHA-1 & Client ID di Google Console";
            if (e.getStatusCode() == 12500) pesan = "Update Google Play Services di HP/Emulator";
            
            Toast.makeText(this, pesan, Toast.LENGTH_LONG).show();
        }
    }

    // 5. Fungsi bantuan untuk mengirim data ke Laravel menggunakan Retrofit
    private void sendTokenToLaravelServer(String idToken) {
        // Ambil data Google Account jika tersedia untuk fallback
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        HashMap<String, String> map = new HashMap<>();
        map.put("id_token", idToken);

        ApiClient.getApiService().verifyGoogleToken(map).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String sanctumToken = null;

                    // Coba ambil token dari struktur 'data' atau langsung dari root
                    if (loginResponse.getData() != null) {
                        sanctumToken = loginResponse.getData().getToken();
                    } else if (loginResponse.getToken() != null) {
                        sanctumToken = loginResponse.getToken();
                    }

                    if (sanctumToken == null) {
                        Log.e("API_ERROR", "Token tidak ditemukan dalam response JSON. Response body: " + response.body().toString());
                        Toast.makeText(LoginActivity.this, "Format response server tidak sesuai (Token missing)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences sharedPref = getSharedPreferences("KantongkuPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("token_akses", sanctumToken);
                    
                    // Simpan data user untuk ditampilkan di Setelan
                    if (loginResponse.getData() != null && loginResponse.getData().getUser() != null) {
                        editor.putString("user_name", loginResponse.getData().getUser().getName());
                        editor.putString("user_email", loginResponse.getData().getUser().getEmail());
                    } else if (account != null) {
                        // Fallback ke data Google jika API tidak mengembalikan objek user
                        editor.putString("user_name", account.getDisplayName());
                        editor.putString("user_email", account.getEmail());
                    }

                    editor.apply();

                    Log.d("API_SUCCESS", "User berhasil diverifikasi!");
                    Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Gagal verifikasi. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " | Error: " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("API_ERROR", errorMsg);
                    Toast.makeText(LoginActivity.this, "Server menolak token (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("NETWORK_ERROR", "Gagal terhubung ke server: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Koneksi ke server gagal!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}