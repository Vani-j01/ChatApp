package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private ImageButton sendMessageButton, sendFilesButton;
    private EditText messageInputText;

    private final List<Messages> messagesList= new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;
    private String saveCurrentTime, saveCurrentDate;
    private String checker= "", myUrl= "";
    private StorageTask uploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;


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
        userLastSeen= findViewById(R.id.chatbar_user_lastseen);
        userImage= findViewById(R.id.chatbar_user_image);
        sendMessageButton= findViewById(R.id.chat_send_message_btn);
        sendFilesButton= findViewById(R.id.chat_send_file_btn);
        messageInputText= findViewById(R.id.input_message);
        loadingBar = new ProgressDialog(this);


        messagesAdapter= new MessagesAdapter(messagesList);
        userMessagesList= (RecyclerView) findViewById(R.id.private_msg_list);
        linearLayoutManager= new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);

        //fetching current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        //Setting the image
//        MyAppGlideModule obj = new MyAppGlideModule();
//        obj.setImage(messageReceiverID, userImage);

        GlideApp.with(getApplicationContext())
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

        displayLastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Documents"
                        }       ;

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File ");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "select Image"),438);
                        }
                        if (which==1){
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "select PDF file"),438);

                        }
                        if (which==2){
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "select MS Word File"),438);

                        }
                    }
                });
                builder.show();
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
                            recreate();
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
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageId",messagePushId);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+ "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef+ "/" + messagePushId, messageTextBody);

            rootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){

                    }
                    else{
                        Toast.makeText(ChatActivity.this,"Error in sending message",Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });



        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode== RESULT_OK && data!=null && data.getData()!=null){

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait while we send your files");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            //Saving the Uri
            fileUri= data.getData();

            if (!checker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Message/"+messageSenderID +"/" + messageReceiverID;
                final String messageReceiverRef = "Message/"+messageReceiverID +"/" + messageSenderID;

                DatabaseReference userMessageKeyReference= rootReference.child("Message")
                        .child(messageSenderRef).child(messageReceiverID).push();

                final String messagePushId = userMessageKeyReference.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+ "."+ checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message",downloadUrl);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("type",checker);
                            messageImageBody.put("from",messageSenderID);
                            messageImageBody.put("to",messageReceiverID);
                            messageImageBody.put("messageId",messagePushId);
                            messageImageBody.put("time",saveCurrentTime);
                            messageImageBody.put("date",saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef+ "/" + messagePushId, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef+ "/" + messagePushId, messageImageBody);

                            rootReference.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                        }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                        });
                    }

                    //Adding Progress bar showing how much upload is left
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                       loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });

            }

            //If file is an image
            else if (checker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Message/"+messageSenderID +"/" + messageReceiverID;
                final String messageReceiverRef = "Message/"+messageReceiverID +"/" + messageSenderID;

                DatabaseReference userMessageKeyReference= rootReference.child("Message")
                        .child(messageSenderRef).child(messageReceiverID).push();

                final String messagePushId = userMessageKeyReference.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+ "."+ "jpg");

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message",myUrl);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("type",checker);
                            messageImageBody.put("from",messageSenderID);
                            messageImageBody.put("to",messageReceiverID);
                            messageImageBody.put("messageId",messagePushId);
                            messageImageBody.put("time",saveCurrentTime);
                            messageImageBody.put("date",saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef+ "/" + messagePushId, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef+ "/" + messagePushId, messageImageBody);

                            rootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                                Toast.makeText(ChatActivity.this,"Image Sent",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this,"Error in sending message",Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });

                        }
                    }
                });

            }
            else {
                loadingBar.dismiss();
                Toast.makeText(ChatActivity.this,"Nothing Selected",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLastSeen(){
        rootReference.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //getting user state and last seen time
                        if (snapshot.child("userState").hasChild("state")) {
                            String state = snapshot.child("userState").child("state").getValue().toString();
                            String date = snapshot.child("userState").child("date").getValue().toString();
                            String time = snapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")){
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline")){
                                String str = "Last Seen: " + date+ " " + time;
                                userLastSeen.setText(str);
                            }
                        }
                        //if user has not updated the app or somehow has not got his info stored
                        else {
                            userLastSeen.setText("Last Seen: " + "Date" + " Time");

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}