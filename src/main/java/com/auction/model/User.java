package com.auction.model;

public abstract class User {
    private String id;
    private String username;
    private String email;

    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    public String getUsername() { return username; }

    public abstract void displayMenu();
}