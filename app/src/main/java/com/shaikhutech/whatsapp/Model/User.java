package com.shaikhutech.whatsapp.Model;

public class User {
    public String name;
    public String id;
    public String profilePicUrl;

    public String status;

    public String phone;


    public User(String name, String id, String profilePicUrl, String status,String phone) {
        this.name = name;
        this.id = id;
        this.profilePicUrl = profilePicUrl;
        this.status = status;
        this.phone = phone;
    }

    public User(){

    }
}
