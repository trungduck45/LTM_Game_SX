package com.btl.gamesxclients;

public class Room {
    private String roomId;
    private String player1Id;
    private String player2Id;
    private String roomStatus;

    public Room(String roomId, String player1Id, String player2Id, String roomStatus) {
        this.roomId = roomId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.roomStatus = roomStatus;
    }

    // Getter và Setter
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId='" + roomId + '\'' +
                ", player1Id='" + player1Id + '\'' +
                ", player2Id='" + player2Id + '\'' +
                ", roomStatus='" + roomStatus + '\'' +
                '}';
    }
}
