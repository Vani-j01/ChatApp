package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import this to customize toolbar
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriends extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerList;
    private DatabaseReference UserReference;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");

        //Initializing the Recycler List that will display all the contacts
        recyclerList = findViewById(R.id.find_friends_recycleview);
        recyclerList.setLayoutManager(new LinearLayoutManager(this));


        toolbar= (Toolbar) findViewById(R.id.findfriends_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsList> options=
                new FirebaseRecyclerOptions.Builder<ContactsList>()
                .setQuery(UserReference, ContactsList.class)
                .build();

        FirebaseRecyclerAdapter<ContactsList, FindFriendsViewHolder> adapter=
                new FirebaseRecyclerAdapter<ContactsList, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull ContactsList model) {
                        holder.username.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        final String visit_user_id;
                        visit_user_id=getRef(position).getKey();

                        //Setting the image
                        MyAppGlideModule obj = new MyAppGlideModule();
                        obj.setImage(visit_user_id,holder.profileImage);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent profileIntent = new Intent(FindFriends.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                       FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };

        recyclerList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView username, userStatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            username= itemView.findViewById(R.id.user_name);
            userStatus= itemView.findViewById(R.id.user_status);
            profileImage= itemView.findViewById(R.id.users_profile_image);
        }
    }


}