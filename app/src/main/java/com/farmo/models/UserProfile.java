package com.farmo.models;

public class UserProfile {

    // ── Header ────────────────────────────────────────────────────────────────
    private String fullName;
    private String profileImageUrl;

    // ── Contact Information ───────────────────────────────────────────────────
    private String mobileNumber;
    private String email;
    private String whatsapp;
    private String facebook;

    // ── Personal Details ──────────────────────────────────────────────────────
    private String  dateOfBirth;
    private String  sex;
    private String  userId;
    private String  userType;
    private boolean isVerified;   // ← true if farmer is verified

    // ── Address ───────────────────────────────────────────────────────────────
    private String province;
    private String district;
    private String municipality;
    private String wardNo;
    private String tole;

    // ── About ─────────────────────────────────────────────────────────────────
    private String about;

    // ─────────────────────────────────────────────────────────────────────────
    public UserProfile() {}

    // ── Getters & Setters — Header ────────────────────────────────────────────

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    // ── Getters & Setters — Contact ───────────────────────────────────────────

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }

    // ── Getters & Setters — Personal ──────────────────────────────────────────

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    // ── Getters & Setters — Address ───────────────────────────────────────────

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }

    public String getWardNo() { return wardNo; }
    public void setWardNo(String wardNo) { this.wardNo = wardNo; }

    public String getTole() { return tole; }
    public void setTole(String tole) { this.tole = tole; }

    // ── Getters & Setters — About ─────────────────────────────────────────────

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "UserProfile{fullName='" + fullName + "', userId='" + userId +
                "', userType='" + userType + "', isVerified=" + isVerified + '}';
    }
}