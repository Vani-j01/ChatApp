package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Group_Chat_Activity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendMsgBtn;
    private EditText userMsg;
    private ScrollView mScrollView;
    private TextView groupMsgDisplay;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference, GroupNameReference, GroupMessageKeyRefernce;

    private String currentGroupName, currentUserId, currentUserName;
    private String currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group__chat_);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        Initializing();
        Log.d("Group Chat", "Back from Initializing");

        GetUserInfo();
        Log.d("Group Chat", "Back from UserInfo");


        SendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMsgToDatabase();
                userMsg.setText("");
            }
        });
    }



    private void SaveMsgToDatabase() {
        String message = userMsg.getText().toString();
        String messageKey = GroupNameReference.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Looks like you forgot your Message", Toast.LENGTH_LONG).show();
        } else {
            Calendar calendarforDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calendarforDate.getTime());

            Calendar calendarforTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm:ss a");
            currentTime = currentTimeFormat.format(calendarforTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameReference.updateChildren(groupMessageKey);

            GroupMessageKeyRefernce = GroupNameReference.child(messageKey);
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            GroupMessageKeyRefernce.updateChildren(messageInfoMap);
        }

    }

    private void GetUserInfo() {

        Log.d("Group Chat", "UserInfo");

        userReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void Initializing() {

        Log.d("Group Chat", "Initializing");
        mToolbar = (Toolbar) findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMsgBtn = (ImageButton) findViewById(R.id.group_chat_sendmsg_btn);
        userMsg = (EditText) findViewById(R.id.group_chat_entertext);
        mScrollView = (ScrollView) findViewById(R.id.group_chat_scrollview);
        groupMsgDisplay =(TextView) findViewById(R.id.group_chat_display);

    }
}