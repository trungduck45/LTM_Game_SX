package com.btl.gamesxclients;

public class UserProfile {
    private String userId;
    private String ingameName;
    private int totalPoint;
    private int rankedPoint;

    // Constructor, getters, and setters
    public UserProfile(String userId, String ingameName, int totalPoint, int rankedPoint) {
        this.ingameName = ingameName;
        this.totalPoint = totalPoint;
        this.rankedPoint = rankedPoint;
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
}