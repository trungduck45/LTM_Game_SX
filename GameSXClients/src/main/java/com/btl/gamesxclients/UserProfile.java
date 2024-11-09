package com.btl.gamesxclients;

public class UserProfile {
    private String userId;
    private String ingameName;
    private int totalPoint;
    private int rankedPoint;
    private String status;

    // Constructor, getters, and setters
    public UserProfile(String userId, String ingameName, int totalPoint, int rankedPoint, String status) {
        this.ingameName = ingameName;
        this.totalPoint = totalPoint;
        this.rankedPoint = rankedPoint;
        this.status = status;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getIngameName() {
        return ingameName;
    }

    public int getTotalPoint() {
        return totalPoint;
    }

    public int getRankedPoint() {
        return rankedPoint;
    }
    public String getStatus(){
        return status;
    }
}