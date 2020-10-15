package com.example.letschat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import javax.sql.StatementEvent;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {


    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference UserProfileImageRef;

    private List<Messages> userMessagesList;

    //Constructor
    public MessagesAdapter (List<Messages> userMessagesList){

        this.userMessagesList= userMessagesList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(View itemView){
            super(itemView);

            senderMessageText= (TextView) itemView.findViewById(R.id.message_sender_text);
            receiverMessageText= (TextView) itemView.findViewById(R.id.message_receiver_text);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_message_layout, parent, false);

        mAuth=FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
    String messageSenderID= mAuth.getCurrentUser().getUid();
    Messages messages = userMessagesList.get(position);

    String fromUserID= messages.getFrom();
    String fromMessageType = messages.getType();

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

    userRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.hasChild("image")){

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

    if (fromMessageType.equals("text")){
        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.senderMessageText.setVisibility(View.INVISIBLE);

        if (fromUserID.equals(messageSenderID)){
            holder.senderMessageText.setVisibility(View.VISIBLE);
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
            holder.senderMessageText.setText(messages.getMessage());
        }
        else {
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
            holder.receiverMessageText.setText(messages.getMessage());
        }
    }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }



}
