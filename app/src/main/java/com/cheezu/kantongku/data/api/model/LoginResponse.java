package com.cheezu.kantongku.data.api.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("token") // Jaga-jaga jika token ada di root
    private String token;
    
    @SerializedName("data")
    private AuthData data;

    public String getStatus() { return status; }
    public String getToken() { return token; }
    public AuthData getData() { return data; }

    public static class AuthData {
        @SerializedName("token")
        private String token;

        @SerializedName("user")
        private User user;

        public String getToken() { return token; }
        public User getUser() { return user; }
    }

    public static class User {
        @SerializedName("name")
        private String name;
        @SerializedName("email")
        private String email;

        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}
