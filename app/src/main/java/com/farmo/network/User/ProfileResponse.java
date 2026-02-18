package com.farmo.network.User;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    private String user_id;
    private String full_name;
    private String address;
    private String phone;
    private String phone2;
    private String user_type;
    private String email;
    private String facebook;
    private String whatsapp;
    private String join_date;
    private String about;
    private String dob;
    private String sex;
    private String profile_picture; // Base64 String
    private String pp_mime_type;

    // Getters
    public String getUserId() { return user_id; }
    public String getFullName() { return full_name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getPhone2() { return phone2; }
    public String getUserType() { return user_type; }
    public String getEmail() { return email; }
    public String getFacebook() { return facebook; }
    public String getWhatsapp() { return whatsapp; }
    public String getJoinDate() { return join_date; }
    public String getAbout() { return about; }
    public String getDob() { return dob; }
    public String getSex() { return sex; }
    public String getProfilePicture() { return profile_picture; }
}
