package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageSenderID, messageReceiverName, messageReceiverStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;


    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private StorageReference UserProfileImageRef;
    private Toolbar chatToolbar;
    private ImageButton sendMessageButton;
    private EditText messageInputText;

    private final List<Messages> messagesList= new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;
    ChildEventListener listener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID= getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName= getIntent().getExtras().get("visit_user_name").toString();

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        mAuth=FirebaseAuth.getInstance();

        messageSenderID= mAuth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();

        chatToolbar= (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionbarView);


        userName = findViewById(R.id.chatbar_user_name);
        userLastSeen= findViewById(R.id.chatbar_user_latseen);
        userImage= findViewById(R.id.chatbar_user_image);
        sendMessageButton=(ImageButton) findViewById(R.id.chat_send_message_btn);
        messageInputText= findViewById(R.id.input_message);

        messagesAdapter= new MessagesAdapter(messagesList);
        userMessagesList= (RecyclerView) findViewById(R.id.private_msg_list);
        linearLayoutManager= new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);


        GlideApp.with(ChatActivity.this)
                .load(UserProfileImageRef.child(messageReceiverID + ".jpg"))
                .fitCenter()
                .placeholder(R.drawable.user_image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(userImage);

        userName.setText(messageReceiverName);




        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i("Chat Activity", "Started");

             listener = new ChildEventListener()
                {

                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Log.i("Chat Activity", "ChildAdded");
                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();

                        //Auto Scroll
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };

        rootReference.child("Message").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(listener);
    }


    @Override
    protected void onPause() {
        super.onPause();
        messagesList.clear();
        rootReference.child("Message").child(messageSenderID).child(messageReceiverID)
                .removeEventListener(listener);
    }

    private void SendMessage(){
        Log.i("Chat Activity", "Button CLicked");
        String msg = messageInputText.getText().toString();

        if (TextUtils.isEmpty(msg)){
            Toast.makeText(this, "Write a message first",Toast.LENGTH_SHORT).show();
        }
        else {
            Log.i("Chat Activity", "else part");
            String messageSenderRef = "Message/"+messageSenderID +"/" + messageReceiverID;
            String messageReceiverRef = "Message/"+messageReceiverID +"/" + messageSenderID;

            DatabaseReference userMessageKeyReference= rootReference.child("Message")
                    .child(messageSenderRef).child(messageReceiverID).push();

            String messagePushId = userMessageKeyReference.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",msg);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+ "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef+ "/" + messagePushId, messageTextBody);

            rootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this,"Message Sent", Toast.LENGTH_SHORT).show();

                    }
                    else{
                        Toast.makeText(ChatActivity.this,"Error in sending message",Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });



        }
    }
}