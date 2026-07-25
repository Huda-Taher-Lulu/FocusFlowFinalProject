package com.example.finalproject_hudalulu_220253814.models;

import java.io.Serializable;

/** Domain model for a registered user account, stored in the Users SQLite database. */
public class User implements Serializable {

    private long id = -1;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String password;

    public User(String fullName, String username, String email, String phone, String address, String password) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}