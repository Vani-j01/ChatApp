package com.example.letschat;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

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
        public ImageView messageSenderImage, messageReceiverImage;

        public MessageViewHolder(View itemView){
            super(itemView);

            senderMessageText= (TextView) itemView.findViewById(R.id.message_sender_text);
            receiverMessageText= (TextView) itemView.findViewById(R.id.message_receiver_text);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverImage= (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderImage= (ImageView) itemView.findViewById(R.id.message_sender_image_view);
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
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
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


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderImage.setVisibility(View.GONE);
        holder.messageReceiverImage.setVisibility(View.GONE);

    if (fromMessageType.equals("text")){

        if (fromUserID.equals(messageSenderID)){
            holder.senderMessageText.setVisibility(View.VISIBLE);
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
            holder.senderMessageText.setText(messages.getMessage()+ "\n\n" + messages.getTime()+ " - " + messages.getDate());
        }
        else {
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
            holder.receiverMessageText.setText(messages.getMessage()+ "\n\n" + messages.getTime()+ " - " + messages.getDate());
        }
    }

    else if (fromMessageType.equals("image")){
        if (fromUserID.equals(messageSenderID)){
            holder.messageSenderImage.setVisibility(View.VISIBLE);

            Picasso.get().load(messages.getMessage()).into(holder.messageSenderImage);
        }
        else {
            holder.messageReceiverImage.setVisibility(View.VISIBLE);
            holder.receiverProfileImage.setVisibility(View.VISIBLE);

            Picasso.get().load(messages.getMessage()).into(holder.messageReceiverImage);
        }
    }

    else {
        if (fromUserID.equals(messageSenderID)){
            holder.messageSenderImage.setVisibility(View.VISIBLE);
            holder.messageSenderImage.setBackgroundResource(R.drawable.email);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        }
        else {
            holder.messageReceiverImage.setVisibility(View.VISIBLE);
            holder.receiverProfileImage.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                    holder.itemView.getContext().startActivity(intent);
                }
            });

            holder.messageReceiverImage.setBackgroundResource(R.drawable.email);
        }
    }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }



}
