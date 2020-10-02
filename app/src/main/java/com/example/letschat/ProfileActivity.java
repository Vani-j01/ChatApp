package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private  String recievedUserId, currentUserId, currentState;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestBtn;
    private DatabaseReference userReferrence, ChatRequestReferrence;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth= FirebaseAuth.getInstance();
        userReferrence= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestReferrence= FirebaseDatabase.getInstance().getReference().child("Chat Requests");


        recievedUserId= getIntent().getExtras().get("visit_user_id").toString();
        currentUserId= mAuth.getCurrentUser().getUid().toString();


        userProfileImage= findViewById(R.id.visit_user_profile_image);
        userProfileName= findViewById(R.id.visit_profile_user_name);
        userProfileStatus=findViewById(R.id.vist_profile_user_status);
        sendMessageRequestBtn= (Button)findViewById(R.id.send_msg_btn);
        currentState= "new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        userReferrence.child(recievedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){

                    String userImage= snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.user_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequest() {
        if(currentUserId.equals(recievedUserId)){
            sendMessageRequestBtn.setVisibility(View.INVISIBLE);

        }
        else{
            sendMessageRequestBtn.setVisibility(View.VISIBLE);

            sendMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestBtn.setEnabled(false);
                    
                    if (currentState.equals("new")){
                        SendChatRequest();
                    }

                    if(currentState.equals("request_sent")){
                        CancelChatRequest();
                    }
                }
            });
        }

    }

    private void CancelChatRequest() {
        ChatRequestReferrence.child(currentUserId).child(recievedUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ChatRequestReferrence.child(recievedUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                sendMessageRequestBtn.setEnabled(true);
                                                currentState="new";
                                                sendMessageRequestBtn.setText("Send Message");
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void SendChatRequest() {
        ChatRequestReferrence.child(currentUserId).child(recievedUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ChatRequestReferrence.child(recievedUserId).child(currentUserId)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                sendMessageRequestBtn.setEnabled(true);
                                                currentState="request_sent";
                                                sendMessageRequestBtn.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}