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

    private  String receivedUserId, currentUserId, currentState;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestBtn, declineRequestBtn;
    private DatabaseReference userReference, ChatRequestReference, ContactsReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Initializing Firebase Related References
        mAuth= FirebaseAuth.getInstance();
        userReference= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestReference= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsReference= FirebaseDatabase.getInstance().getReference().child("Contacts");

        //Receiving user ID from FindFriends Activity
        receivedUserId= getIntent().getExtras().get("visit_user_id").toString();
        currentUserId= mAuth.getCurrentUser().getUid().toString();


        userProfileImage= findViewById(R.id.visit_user_profile_image);
        userProfileName= findViewById(R.id.visit_profile_user_name);
        userProfileStatus=findViewById(R.id.vist_profile_user_status);
        sendMessageRequestBtn= (Button)findViewById(R.id.send_msg_btn);
        declineRequestBtn= (Button)findViewById(R.id.decline_msg_btn);

        currentState= "new";

        RetrieveUserInfo();
    }


    //Method to retrieve users Data from Database
    private void RetrieveUserInfo() {
        userReference.child(receivedUserId).addValueEventListener(new ValueEventListener() {
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

        //Setting Button Text
        ChatRequestReference.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            if (snapshot.hasChild(receivedUserId)) {
                                String request_type = snapshot.child(receivedUserId).child("request_type").getValue().toString();

                                if (request_type.equals("sent")) {
                                    currentState = "request_sent";
                                    sendMessageRequestBtn.setText("Cancel Chat Request");
                                } else if (request_type.equals("received")) {
                                    currentState = "request_received";
                                    sendMessageRequestBtn.setText("Accept Chat Request");

                                    declineRequestBtn.setVisibility(View.VISIBLE);
                                    declineRequestBtn.setEnabled(true);

                                    declineRequestBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            CancelChatRequest();
                                        }
                                    });
                                }
                            }
                        }
                            else {
                                ContactsReference.child(currentUserId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.hasChild(receivedUserId)) {
                                                    currentState = "friends";
                                                    sendMessageRequestBtn.setText("Remove this Contact");
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            }
                        }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        if(currentUserId.equals(receivedUserId)){
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
                    if (currentState.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(currentState.equals("friends")){
                        RemoveContact();
                    }
                }
            });
        }



    }

    private void RemoveContact() {
        ContactsReference.child(currentUserId).child(receivedUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ContactsReference.child(receivedUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                sendMessageRequestBtn.setEnabled(true);
                                                currentState="new";
                                                sendMessageRequestBtn.setText("Send Message");


                                                declineRequestBtn.setVisibility(View.INVISIBLE);
                                                declineRequestBtn.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {

        ContactsReference.child(currentUserId).child(receivedUserId)
                .child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            ContactsReference.child(receivedUserId).child(currentUserId)
                                    .child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                //Removing the chat request sent initially
                                                //After the receiver has accepted it
                                                ChatRequestReference.child(currentUserId).child(receivedUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){
                                                                    ChatRequestReference.child(receivedUserId).child(currentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    sendMessageRequestBtn.setEnabled(true);
                                                                                    currentState= "friends";
                                                                                    sendMessageRequestBtn.setText("Remove this Contact");

                                                                                    declineRequestBtn.setVisibility(View.INVISIBLE);
                                                                                    declineRequestBtn.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void CancelChatRequest() {
        ChatRequestReference.child(currentUserId).child(receivedUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ChatRequestReference.child(receivedUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                sendMessageRequestBtn.setEnabled(true);
                                                currentState="new";
                                                sendMessageRequestBtn.setText("Send Message");


                                                declineRequestBtn.setVisibility(View.INVISIBLE);
                                                declineRequestBtn.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void SendChatRequest() {
        ChatRequestReference.child(currentUserId).child(receivedUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ChatRequestReference.child(receivedUserId).child(currentUserId)
                                    .child("request_type").setValue("received")
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