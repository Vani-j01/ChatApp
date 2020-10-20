package com.example.letschat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.GroupMessageViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference UserProfileImageRef;

    private List<Messages> userMessagesList;

    //Constructor
    public GroupMessagesAdapter (List<Messages> userMessagesList){

        this.userMessagesList= userMessagesList;
    }




    public class GroupMessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText, senderFile, receiverFile;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderImage, messageReceiverImage;

        public GroupMessageViewHolder(View itemView){
            super(itemView);

            senderMessageText= (TextView) itemView.findViewById(R.id.message_sender_text);
            receiverMessageText= (TextView) itemView.findViewById(R.id.message_receiver_text);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverImage= (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderImage= (ImageView) itemView.findViewById(R.id.message_sender_image_view);
            senderFile= itemView.findViewById(R.id.message_sender_file);
            receiverFile= itemView.findViewById(R.id.message_receiver_file);

        }


    }



    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //getting the custom message layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_message_layout, parent, false);

        mAuth=FirebaseAuth.getInstance();


        return new GroupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
        String messageSenderID= mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        //Setting the image
        MyAppGlideModule obj = new MyAppGlideModule();
        obj.setImage(messages.getFrom(),holder.receiverProfileImage);

        String fromUserID= messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderImage.setVisibility(View.GONE);
        holder.messageReceiverImage.setVisibility(View.GONE);
        holder.senderFile.setVisibility(View.GONE);
        holder.receiverFile.setVisibility(View.GONE);


            if (fromUserID.equals(messageSenderID)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setText(messages.getMessage()+ "\n\n" + messages.getTime()+ " - " + messages.getDate());
            }
            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setText(messages.getName().toUpperCase()+"\n\n"+messages.getMessage()+ "\n\n" + messages.getTime()+ " - " + messages.getDate());
            }
        }



    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
