package com.auction.server.service;

import com.auction.common.model.Admin;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.User;

import java.util.HashMap;
import java.util.Map;

public class FallbackUserStore {
    private static final Map<String, User> USERS = new HashMap<>();

    static {
        Admin admin = new Admin("BD500001", "Hades", "123456", 0);
        admin.setRole("ADMIN");

        Seller seller = new Seller("BD500002", "Hephaestus", "SELLER", "123456", 500000);
        seller.setRole("SELLER");

        Bidder bidder = new Bidder("BD500003", "Cerberus", "BIDDER", "123456", 300000);
        bidder.setRole("BIDDER");

        USERS.put("admin", admin);
        USERS.put("seller", seller);
        USERS.put("bidder", bidder);

        System.out.println("✅ Fallback users loaded: " + USERS.keySet());
    }

    public static User login(String username, String password) {
        if (username == null || password == null) return null;

        String normalizedUsername = username.trim().toLowerCase();
        User user = USERS.get(normalizedUsername);

        System.out.println("Fallback lookup username = " + normalizedUsername);
        System.out.println("Fallback found user = " + (user == null ? "null" : user.getUsername()));
        System.out.println("Fallback stored password = " + (user == null ? "null" : user.getPassword()));

        if (user != null && password.equals(user.getPassword())) {
            return cloneUser(user);
        }

        return null;
    }

    public static User getUserById(String userId) {
        for (User user : USERS.values()) {
            if (user.getId().equals(userId)) {
                return cloneUser(user);
            }
        }
        return null;
    }

    public static boolean resetPassword(String username, String newPassword) {
        if (username == null || newPassword == null) return false;

        User user = USERS.get(username.trim().toLowerCase());
        if (user == null) return false;

        user.setPassword(newPassword);
        return true;
    }

    public static boolean register(String username, String password, String role) {
        if (username == null || password == null) return false;

        String normalizedUsername = username.trim().toLowerCase();
        if (USERS.containsKey(normalizedUsername)) return false;

        String id = "BD5" + String.format("%05d", USERS.size() + 10);

        User user;
        if ("ADMIN".equalsIgnoreCase(role)) {
            user = new Admin(id, normalizedUsername, password, 999999);
            user.setRole("ADMIN");
        } else if ("SELLER".equalsIgnoreCase(role)) {
            user = new Seller(id, normalizedUsername, "SELLER", password, 100000);
            user.setRole("SELLER");
        } else {
            user = new Bidder(id, normalizedUsername, "BIDDER", password, 100000);
            user.setRole("BIDDER");
        }

        USERS.put(normalizedUsername, user);
        return true;
    }

    private static User cloneUser(User user) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            Admin clone = new Admin(user.getId(), user.getUsername(), user.getPassword(), user.getBalance());
            clone.setRole("ADMIN");
            return clone;
        } else if ("SELLER".equalsIgnoreCase(user.getRole())) {
            Seller clone = new Seller(user.getId(), user.getUsername(), "SELLER", user.getPassword(), user.getBalance());
            clone.setRole("SELLER");
            return clone;
        } else {
            Bidder clone = new Bidder(user.getId(), user.getUsername(), "BIDDER", user.getPassword(), user.getBalance());
            clone.setRole("BIDDER");
            return clone;
        }
    }
}