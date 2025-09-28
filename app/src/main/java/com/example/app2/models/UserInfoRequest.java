package com.example.app2.models;

public class UserInfoRequest {
    private String username;
    private String email;
    private String name;

    public UserInfoRequest() {}

    public UserInfoRequest(String username, String email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getId() { return name; }
}
