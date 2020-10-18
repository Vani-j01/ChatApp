package com.example.letschat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Requests#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Requests extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View RequestFragView;

    private DatabaseReference UserReference;
    private DatabaseReference ChatRequestReference, ContactsReference;
    private FirebaseAuth mAuth;
    String currentUser;

    private RecyclerView mRequestList;

    public Requests() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Requests.
     */
    // TODO: Rename and change types and number of parameters
    public static Requests newInstance(String param1, String param2) {
        Requests fragment = new Requests();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestList = RequestFragView.findViewById(R.id.requests_list);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid().toString();
        ChatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsReference= FirebaseDatabase.getInstance().getReference().child("Contacts");



        return RequestFragView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<ContactsList>()
                        .setQuery(ChatRequestReference.child(currentUser), ContactsList.class)
                        .build();

        FirebaseRecyclerAdapter<ContactsList, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<ContactsList, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull ContactsList model) {

                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_reject_btn).setVisibility(View.VISIBLE);

                        final String users_ids = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {
                                    String type = snapshot.getValue().toString();
                                    if (type.equals("received")) {

                                        UserReference.child(users_ids).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.hasChild("image")) {
                                                    String profileimage = snapshot.child("image").getValue().toString();

                                                    //Setting the image
                                                    MyAppGlideModule obj = new MyAppGlideModule();
                                                    obj.setImage(users_ids,holder.profileImage);
                                                    //Picasso.get().load(profileimage).into(holder.profileImage)
                                                }

                                                final String profileName = snapshot.child("name").getValue().toString();

                                                holder.userName.setText(profileName);
                                                holder.userStatus.setText("wants to be friends with you");

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]= new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Decline"
                                                                };
                                                        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                                                        builder.setTitle( profileName+"'s Chat Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                if (which == 0){

                                                                    ContactsReference.child(currentUser).child(users_ids).child("Contact").setValue("saved")
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        ContactsReference.child(users_ids).child(currentUser).child("Contact").setValue("saved")
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()){

                                                                                                            ChatRequestReference.child(currentUser).child(users_ids).removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if (task.isSuccessful()){
                                                                                                                                ChatRequestReference.child(users_ids).child(currentUser).removeValue()
                                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                            @Override
                                                                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                                                                if (task.isSuccessful()){
                                                                                                                                                    Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                                                }
                                                                            });
                                                                }

                                                                //Declined
                                                                if (which == 1){
                                                                    ChatRequestReference.child(currentUser).child(users_ids).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        ChatRequestReference.child(users_ids).child(currentUser).removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if (task.isSuccessful()){
                                                                                                            Toast.makeText(getContext(), "Request Declined", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                    //Displaying the Sent requests
                                    else if (type.equals("sent")){
                                        //To show that you sent a request
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                        request_sent_btn.setText("Request Sent");

                                        //Hidding this button
                                        holder.itemView.findViewById(R.id.request_reject_btn).setVisibility(View.INVISIBLE);

                                        UserReference.child(users_ids).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.hasChild("image")) {
                                                    String profileimage = snapshot.child("image").getValue().toString();

                                                    //Setting the image
                                                    MyAppGlideModule obj = new MyAppGlideModule();
                                                    obj.setImage(users_ids,holder.profileImage);

                                                    //Picasso.get().load(profileimage).into(holder.profileImage)
                                                }

                                                //Name of whom request has been sent
                                                final String profileName = snapshot.child("name").getValue().toString();

                                                holder.userName.setText(profileName);
                                                holder.userStatus.setText("you have sent request to: "+profileName );

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]= new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"
                                                                };
                                                        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                                                        builder.setTitle( "Already Sent a Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                //Cancelling the sent Request
                                                                if (which == 0){
                                                                    ChatRequestReference.child(currentUser).child(users_ids).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        ChatRequestReference.child(users_ids).child(currentUser).removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if (task.isSuccessful()){
                                                                                                            Toast.makeText(getContext(), "Request Canceled", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate((R.layout.user_display_layout), parent, false);
                        RequestViewHolder viewHolder = new RequestViewHolder(view);
                        return viewHolder;
                    }
                };

        mRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button accept_btn, decline_btn;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage= itemView.findViewById(R.id.users_profile_image);
            //Add for Image

            accept_btn = itemView.findViewById(R.id.request_accept_btn);
            decline_btn = itemView.findViewById(R.id.request_reject_btn);
        }
    }
}