package com.farmo.network.auth;

import com.google.gson.annotations.SerializedName;

public class TokenLoginRequest {
    @SerializedName("token")
    private String token;
    
    @SerializedName("refresh_token")
    private String refreshToken;
    
    @SerializedName("user_id")
    private String userId;

    
    @SerializedName("device_info")
    private String deviceInfo;

    public TokenLoginRequest(String token, String refreshToken, String userId,  String deviceInfo) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.deviceInfo = deviceInfo;
    }
}
