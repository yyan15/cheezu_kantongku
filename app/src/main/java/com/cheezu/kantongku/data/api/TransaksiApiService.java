package com.cheezu.kantongku.data.api;

import com.cheezu.kantongku.data.api.model.Transaksi;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TransaksiApiService {

    // GET /api/transaksi
    @GET("transaksi")
    Call<ApiResponse.TransaksiList> getAllTransaksi();

    // GET /api/transaksi/bulan-ini
    @GET("transaksi/bulan-ini")
    Call<ApiResponse.DashboardResponse> getTransaksiBulanIni();

    // GET /api/transaksi/statistik
    @GET("transaksi/statistik")
    Call<ApiResponse.StatistikResponse> getStatistik();

    // GET /api/transaksi/search?q=keyword
    @GET("transaksi/search")
    Call<ApiResponse.TransaksiList> searchTransaksi(@Query("q") String keyword);

    // POST /api/transaksi
    @POST("transaksi")
    Call<ApiResponse.TransaksiSingle> tambahTransaksi(@Body Transaksi transaksi);

    // GET /api/transaksi/{id}
    @GET("transaksi/{id}")
    Call<ApiResponse.TransaksiSingle> getTransaksiById(@Path("id") int id);

    // PUT /api/transaksi/{id}
    @PUT("transaksi/{id}")
    Call<ApiResponse.TransaksiSingle> updateTransaksi(@Path("id") int id, @Body Transaksi transaksi);

    // DELETE /api/transaksi/{id}
    @DELETE("transaksi/{id}")
    Call<ApiResponse.GeneralResponse> deleteTransaksi(@Path("id") int id);
}