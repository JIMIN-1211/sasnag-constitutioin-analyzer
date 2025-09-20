package com.example.app.models;

public class UserInfoRequest {
    private String name;
    private String phone;
    private String verificationCode;
    private String gender;
    private String password;

    public UserRequest(String name, String phone, String verificationCode, String gender, String password) {
        this.name = name;
        this.phone = phone;
        this.verificationCode = verificationCode;
        this.gender = gender;
        this.password = password;
    }

    // Getter & Setter (필요시 자동 생성)
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getVerificationCode() { return verificationCode; }
    public String getGender() { return gender; }
    public String getPassword() { return password; }
}
