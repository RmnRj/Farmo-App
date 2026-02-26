package com.farmo.activities.commonActivities;

public class Connection {
    public enum Status { CONNECTED, PENDING, SENT }

    private final String userId;
    private final String fullName;
    private final String profilePic;
    private Status status;

    public Connection(String userId, String fullName, String profilePic, Status status) {
        this.userId     = userId;
        this.fullName   = fullName;
        this.profilePic = profilePic;
        this.status     = status;
    }

    public String getUserId()     { return userId; }
    public String getFullName()   { return fullName; }
    public String getProfilePic() { return profilePic; }
    public Status getStatus()     { return status; }
    public void   setStatus(Status s) { this.status = s; }
}