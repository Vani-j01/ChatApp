package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userDisplayImage;
    private ImageView userdemoDisplayImage;
    private String currentUserId;
    private static final int GalleryPick = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference RootReference;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");
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
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(Settings.this); // will use CROP_IMAGE_ACTIVITY_REQUEST_CODE

            }
        });
    }

    private void RetrieveUserData() {

        RootReference.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if ((snapshot.exists()) && (snapshot.hasChild("username")) && (snapshot.hasChild("image"))) {

                            String retrieveUserName = snapshot.child("username").getValue().toString();
                            String retrieveUserStatus = snapshot.child("status").getValue().toString();
                            Object retrieveprofileImage = snapshot.child("image").getValue();


                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);




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


    private void Initializing() {
        UpdateAccountSettings = findViewById(R.id.update_settings);
        userName = findViewById(R.id.display_name_enter);
        userStatus = findViewById(R.id.status_enter);
        userDisplayImage = (CircleImageView) findViewById(R.id.profile_image);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootReference = FirebaseDatabase.getInstance().getReference();


    }


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

            RootReference.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(Settings.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(Settings.this, "Error :" + error, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait while your Image is Uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();
                Log.d("Setting", "onActivityResult: " + resultUri);

                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                mAuth = FirebaseAuth.getInstance();




                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(Settings.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            RootReference.child("Users").child(currentUserId).child("image")
                                    .setValue(resultUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loadingBar.dismiss();
                                                Toast.makeText(Settings.this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();

                                            } else {
                                                loadingBar.dismiss();
                                                String error = task.getException().toString();
                                                Toast.makeText(Settings.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        } else {
                            loadingBar.dismiss();
                            String errormsg = task.getException().toString();
                            Toast.makeText(Settings.this, "Error:" + errormsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                loadingBar.dismiss();
                Exception error = result.getError();
                Toast.makeText(Settings.this, "Error:" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }


}