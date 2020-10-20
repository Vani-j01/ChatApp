package com.example.letschat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.security.AccessControlContext;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

//import this to customize toolbar
import androidx.appcompat.widget.Toolbar;

import static java.security.AccessController.getContext;

public class Settings extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userDisplayImage;
    private String currentUserId;
    private static final int GalleryPick = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference RootReference;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    Toolbar settingsToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        //Setting up the Toolbar
        settingsToolbar= (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar = new ProgressDialog(this);
        Initializing();


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        RetrieveUserData();

        //Letting User Chose Image from Gallery
        userDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent for opening images files from the phone
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GalleryPick);

            }
        });
    }


    private void Initializing() {
        UpdateAccountSettings = findViewById(R.id.update_settings);
        userName = findViewById(R.id.display_name_enter);
        userStatus = findViewById(R.id.status_enter);
        userDisplayImage = (CircleImageView) findViewById(R.id.profile_image);
        RootReference = FirebaseDatabase.getInstance().getReference();

    }


    //Method to Get user Data from Database and display it
    private void RetrieveUserData() {

        RootReference.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //Checking if data exists
                        if ((snapshot.exists()) && (snapshot.hasChild("username")) && (snapshot.hasChild("image"))) {

                            String retrieveUserName = snapshot.child("username").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                            //Setting the image
                            MyAppGlideModule obj = new MyAppGlideModule();
                            obj.setImage(currentUserId, userDisplayImage);


                        } else if ((snapshot.exists()) && (snapshot.hasChild("username"))) {
                            String retrieveUserName = snapshot.child("username").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                        } else {
                            Toast.makeText(Settings.this, "Please Set & Update profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }




    //Method to update User info in Database
    //Image updation not required as it saves automatically on selection
    private void updateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please Enter Username", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(this, "Please Update Status", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Map<String, Object> profileMap = new HashMap<String, Object>();
            profileMap.put("uid", currentUserId);
            profileMap.put("username", setUserName);
            profileMap.put("status", setUserStatus);

            RootReference.child("Users").child(currentUserId).updateChildren(profileMap)    //Updating the child nodes
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Settings.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(Settings.this, "Error :" + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }


    //Method to Crop and save image to database
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Checking if an image has been chosen
        //Allows only one image to be chosen
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            //Opening Activity to Crop the Image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                //Creating a file path for the image
                //Use the same path each time for one user so that the previous image get's overridden by the new one
                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(Settings.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                            RootReference.child("Users").child(currentUserId).child("image")
                                    .setValue("Has Profile Image");

                            RetrieveUserData();
                        } else {
                            String errormsg = task.getException().toString();
                            Toast.makeText(Settings.this, "Error:" + errormsg, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }


    }


}