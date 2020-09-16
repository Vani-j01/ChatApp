package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userDisplayImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRefference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Initializing();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });
    }



    private void Initializing() {
        UpdateAccountSettings = findViewById(R.id.update_settings);
        userName = findViewById(R.id.display_name_enter);
        userStatus = findViewById(R.id.status_enter);
        userDisplayImage= findViewById(R.id.profile_image);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRefference = FirebaseDatabase.getInstance().getReference();
    }



    private void updateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please Enter Username", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please Update Status", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            Map<String, Object> profileMap = new HashMap<String, Object>();
                profileMap.put("uid", currentUserId);
                profileMap.put("username", setUserName);
                profileMap.put("status", setUserStatus);

                RootRefference.child("Users").child(currentUserId).updateChildren(profileMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    SendUserToMainActivity();
                                    Toast.makeText(Settings.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    String error = task.getException().toString();
                                    Toast.makeText(Settings.this, "Error :"+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(Settings.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}