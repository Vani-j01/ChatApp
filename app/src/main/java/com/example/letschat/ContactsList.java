package com.example.letschat;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class ContactsList {


    //ToDo: Add for Image

    public String name, status;
    public String TAG= "$$$";

    //Empty Constructor
    public  ContactsList(){

    }

    public ContactsList(String name, String status) {
        this.name = name;
        Log.d(TAG, "######"+name);
        this.status = status;
        Log.d(TAG, "######"+status);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



}
