package com.cheezu.kantongku.data.api;

import com.cheezu.kantongku.data.api.model.LoginResponse;
import com.cheezu.kantongku.data.api.model.Transaksi;

import java.util.HashMap;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TransaksiApiService {

    // Auth Google
    @POST("api/auth/google/verify")
    Call<LoginResponse> verifyGoogleToken(@Body HashMap<String, String> body);

    // GET /api/transaksi
    @GET("api/transaksi")
    Call<ApiResponse.TransaksiList> getAllTransaksi();

    // GET /api/transaksi/bulan-ini
    @GET("api/transaksi/bulan-ini")
    Call<ApiResponse.DashboardResponse> getTransaksiBulanIni();

    // GET /api/transaksi/statistik
    @GET("api/transaksi/statistik")
    Call<ApiResponse.StatistikResponse> getStatistik();

    // GET /api/transaksi/search?q=keyword
    @GET("api/transaksi/search")
    Call<ApiResponse.TransaksiList> searchTransaksi(@Query("q") String keyword);

    // POST /api/transaksi
    @POST("api/transaksi")
    Call<ApiResponse.TransaksiSingle> tambahTransaksi(@Body Transaksi transaksi);

    // GET /api/transaksi/{id}
    @GET("api/transaksi/{id}")
    Call<ApiResponse.TransaksiSingle> getTransaksiById(@Path("id") int id);

    // PUT /api/transaksi/{id}
    @PUT("api/transaksi/{id}")
    Call<ApiResponse.TransaksiSingle> updateTransaksi(@Path("id") int id, @Body Transaksi transaksi);

    // DELETE /api/transaksi/{id}
    @DELETE("api/transaksi/{id}")
    Call<ApiResponse.GeneralResponse> deleteTransaksi(@Path("id") int id);
}
