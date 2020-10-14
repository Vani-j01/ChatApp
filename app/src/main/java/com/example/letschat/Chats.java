package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Chats#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Chats extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private  View ChatsView;
    private RecyclerView chat_list;
    private String currentUserId;

    private DatabaseReference chats_Reference, users_Reference;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;

    public Chats() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Chats.
     */
    // TODO: Rename and change types and number of parameters
    public static Chats newInstance(String param1, String param2) {
        Chats fragment = new Chats();
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
        ChatsView= inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chats_Reference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        users_Reference= FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        chat_list = ChatsView.findViewById(R.id.chats_list);
        chat_list.setLayoutManager(new LinearLayoutManager(getContext()));

        return ChatsView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsList> options =
                new FirebaseRecyclerOptions.Builder<ContactsList>()
                .setQuery(chats_Reference,ContactsList.class)
                .build();

        FirebaseRecyclerAdapter<ContactsList,ChatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<ContactsList, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull ContactsList model) {

                        final String usersIds = getRef(position).getKey();

                        users_Reference.child(usersIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {
                                    if (snapshot.hasChild("image")) {
                                        String profileimage = snapshot.child("image").getValue().toString();

                                        GlideApp.with(getContext())
                                                .load(UserProfileImageRef.child(usersIds + ".jpg"))
                                                .fitCenter()
                                                .placeholder(R.drawable.user_image)
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .into(holder.userImage);

                                    }

                                    final String profileName = snapshot.child("name").getValue().toString();
                                    final String profileStatus = snapshot.child("status").getValue().toString();
                                    holder.userName.setText(profileName);
                                    holder.userStatus.setText("Last Seen:" + "\n" + "Date" + " Time");

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",usersIds);
                                            chatIntent.putExtra("visit_user_name",profileName);
                                            chatIntent.putExtra("visit_user_status",profileStatus);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.user_display_layout, parent, false);

                        return new ChatsViewHolder(view);
                        }
                };

        chat_list.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView userImage;
        TextView userStatus, userName;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage= itemView.findViewById(R.id.users_profile_image);
            userName= itemView.findViewById(R.id.user_name);
            userStatus= itemView.findViewById(R.id.user_status);
        }
    }
}