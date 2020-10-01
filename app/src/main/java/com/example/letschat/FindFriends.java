package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import this to customize toolbar
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriends extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerList;
    private DatabaseReference UserReferrence;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UserReferrence = FirebaseDatabase.getInstance().getReference().child("Users");

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
                .setQuery(UserReferrence, ContactsList.class)
                .build();

        FirebaseRecyclerAdapter<ContactsList, FindFriendsViewHolder> adapter=
                new FirebaseRecyclerAdapter<ContactsList, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull ContactsList model) {
                        holder.username.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        //Picasso.get().load(model.getImage()).into(holder.profileImage);
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
        //CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            username= itemView.findViewById(R.id.user_name);
            userStatus= itemView.findViewById(R.id.user_status);
           // profileImage= itemView.findViewById(R.id.users_profile_image);
        }
    }


}