package com.lostnfound.app;

public class UserModel {
    private String uid, name, email, department;
    public UserModel() {}
    public UserModel(String uid, String name, String email, String department) {
        this.uid = uid; this.name = name; this.email = email; this.department = department;
    }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}