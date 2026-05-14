package com.cheezu.kantongku.data.api.model;

import com.google.gson.annotations.SerializedName;

public class Transaksi {

    @SerializedName("id")
    private int id;

    @SerializedName("judul")
    private String judul;

    @SerializedName("kategori")
    private String kategori;

    @SerializedName("tipe")
    private String tipe;           // "pengeluaran" atau "pemasukan"

    @SerializedName("nominal")
    private double nominal;

    @SerializedName("catatan")
    private String catatan;

    @SerializedName("tanggal")
    private String tanggal;        // format: "2026-05-14"

    // ─── Constructor ────────────────────────────────────────
    public Transaksi(String judul, String kategori, String tipe,
                     double nominal, String catatan, String tanggal) {
        this.judul    = judul;
        this.kategori = kategori;
        this.tipe     = tipe;
        this.nominal  = nominal;
        this.catatan  = catatan;
        this.tanggal  = tanggal;
    }

    // ─── Getter & Setter ────────────────────────────────────
    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public String getJudul()            { return judul; }
    public void setJudul(String j)      { this.judul = j; }

    public String getKategori()         { return kategori; }
    public void setKategori(String k)   { this.kategori = k; }

    public String getTipe()             { return tipe; }
    public void setTipe(String t)       { this.tipe = t; }

    public double getNominal()          { return nominal; }
    public void setNominal(double n)    { this.nominal = n; }

    public String getCatatan()          { return catatan; }
    public void setCatatan(String c)    { this.catatan = c; }

    public String getTanggal()          { return tanggal; }
    public void setTanggal(String t)    { this.tanggal = t; }
}