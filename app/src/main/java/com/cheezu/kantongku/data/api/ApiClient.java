package com.cheezu.kantongku.data.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://54.210.147.159";
    private static Retrofit retrofit = null;
    private static Context mContext;

    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            // Interceptor for Authorization Token
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Accept", "application/json");

                if (mContext != null) {
                    SharedPreferences sharedPref = mContext.getSharedPreferences("KantongkuPrefs", Context.MODE_PRIVATE);
                    String token = sharedPref.getString("token_akses", null);
                    if (token != null) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Buat instance ApiService
    public static TransaksiApiService getApiService() {
        return getClient().create(TransaksiApiService.class);
    }
}
