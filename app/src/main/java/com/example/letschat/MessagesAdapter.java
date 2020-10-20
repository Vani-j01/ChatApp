package com.example.letschat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.sql.StatementEvent;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.core.content.ContextCompat.startActivity;

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

        public TextView senderMessageText, receiverMessageText, senderFile, receiverFile;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderImage, messageReceiverImage;

        public MessageViewHolder(View itemView){
            super(itemView);

            senderMessageText= (TextView) itemView.findViewById(R.id.message_sender_text);
            receiverMessageText= (TextView) itemView.findViewById(R.id.message_receiver_text);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverImage= (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderImage= (ImageView) itemView.findViewById(R.id.message_sender_image_view);
            senderFile= (TextView) itemView.findViewById(R.id.message_sender_file);
            receiverFile= (TextView) itemView.findViewById(R.id.message_receiver_file);
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
    final Messages messages = userMessagesList.get(position);


    String fromUserID= messages.getFrom();
    String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

    userRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.hasChild("image")){
                //Setting the image
                MyAppGlideModule obj = new MyAppGlideModule();
                obj.setImage(messages.getFrom(),holder.receiverProfileImage);

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
        holder.senderFile.setVisibility(View.GONE);
        holder.receiverFile.setVisibility(View.GONE);

    if (fromMessageType.equals("text")){
        String s= messages.getMessage()+ "\n\n" + messages.getTime()+ " - " + messages.getDate();

        if (fromUserID.equals(messageSenderID)){
            holder.senderMessageText.setVisibility(View.VISIBLE);
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
            holder.senderMessageText.setText(s);
        }
        else {
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
            holder.receiverMessageText.setText(s);
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

            holder.senderFile.setVisibility(View.VISIBLE);
            holder.senderFile.setBackgroundResource(R.drawable.sender_message_layout);


        }
        else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")){

            holder.receiverFile.setVisibility(View.VISIBLE);
            holder.receiverFile.setBackgroundResource(R.drawable.receiver_message_layout);
            holder.receiverProfileImage.setVisibility(View.VISIBLE);

        }
    }


    if (fromUserID.equals(messageSenderID)){
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx") ) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Download and view this Document",
                                    "Cancel",
                                    "Delete for everyone"
                            };

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (which == 0) {
                                deleteSentMessages(position, holder);

                            } else if (which == 1) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                holder.itemView.getContext().startActivity(intent);
                            } else if (which == 3) {
                                deleteMessagesforEveryone(position, holder);
                            }
                        }
                    });
                    builder.show();
                }

                else  if (userMessagesList.get(position).getType().equals("text") ){
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        "Delete for everyone"
                                };

                        AlertDialog.Builder builder= new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which==0){
                                    deleteSentMessages(position, holder);
                                }
                                else if (which==2){
                                    deleteMessagesforEveryone(position, holder);

                                }
                            }
                        });
                        builder.show();
                    }

                else  if (userMessagesList.get(position).getType().equals("image") ){
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "View this Image",
                                    "Cancel",
                                    "Delete for everyone"
                            };

                    AlertDialog.Builder builder= new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (which==0){
                               deleteSentMessages(position, holder);

                            }
                            else if (which==1){
                                Intent intent= new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                intent.putExtra("url",userMessagesList.get(position).getMessage());
                                holder.itemView.getContext().startActivity(intent);

                            }
                            else if (which==3){
                                deleteMessagesforEveryone(position, holder);
                            }
                        }
                    });
                    builder.show();
                }


                }

        });
    }

    else {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx") ) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Download and view this Document",
                                    "Cancel"
                            };

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (which == 0) {
                                deleteReceivedMessages(position, holder);

                            } else if (which == 1) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                holder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();
                }

                else  if (userMessagesList.get(position).getType().equals("text") ){
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "Cancel"
                            };

                    AlertDialog.Builder builder= new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (which==0){
                                deleteReceivedMessages(position, holder);

                            }
                        }
                    });
                    builder.show();
                }

                else  if (userMessagesList.get(position).getType().equals("image") ){
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete for me",
                                    "View this Image",
                                    "Cancel"
                            };

                    AlertDialog.Builder builder= new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (which==0){
                                deleteReceivedMessages(position, holder);
                                Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if (which==1){
                                Intent intent= new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                intent.putExtra("url",userMessagesList.get(position).getMessage());
                                holder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();
                }


            }

        });
    }
    }




    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private  void deleteSentMessages(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Message").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(),"Message deleted",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(),"Error in deleting",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private  void deleteReceivedMessages(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Message").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(),"Message deleted",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(),"Error in deleting",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private  void deleteMessagesforEveryone(final int position, final MessageViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Message").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    rootRef.child("Message").child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(),"Message deleted",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(holder.itemView.getContext(),"Error in deleting",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
