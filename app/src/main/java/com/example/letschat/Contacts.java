package com.example.letschat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Contacts#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Contacts extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private View ContactView;
    private RecyclerView mContactList;
    private DatabaseReference ContactsReference, UserReference;
    private FirebaseAuth mAuth;
    String currentUser;


    public Contacts() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Contacts.
     */
    // TODO: Rename and change types and number of parameters
    public static Contacts newInstance(String param1, String param2) {
        Contacts fragment = new Contacts();
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
       ContactView = inflater.inflate(R.layout.fragment_contacts, container, false);

       mContactList = ContactView.findViewById(R.id.contacts_list);
       mContactList.setLayoutManager(new LinearLayoutManager(getContext()));

       mAuth = FirebaseAuth.getInstance();
       currentUser = mAuth.getCurrentUser().getUid().toString();
        ContactsReference= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUser);
       UserReference= FirebaseDatabase.getInstance().getReference().child("Users");

       return ContactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<ContactsList>()
                .setQuery(ContactsReference, ContactsList.class)
                .build();
        FirebaseRecyclerAdapter<ContactsList, ContactsViewHolder> adapter=
                new FirebaseRecyclerAdapter<ContactsList, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull ContactsList model) {

                        String users_ids= getRef(position).getKey();
                        UserReference.child(users_ids).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.hasChild("image")){
                                    String profileimage= snapshot.child("image").getValue().toString();
                                    String profileName= snapshot.child("name").getValue().toString();
                                    String profileStatus= snapshot.child("status").getValue().toString();

                                    holder.userName.setText(profileName);
                                    holder.userStatus.setText(profileStatus);
                                    //Picasso.get().load(profileimage).into(holder.profileImage)
                                }
                                else{
                                    String profileName= snapshot.child("name").getValue().toString();
                                    String profileStatus= snapshot.child("status").getValue().toString();

                                    holder.userName.setText(profileName);
                                    holder.userStatus.setText(profileStatus);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate((R.layout.user_display_layout),parent,false);
                        ContactsViewHolder viewHolder= new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };

        mContactList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userStatus= itemView.findViewById(R.id.user_status);
            //Add for Image
        }
    }
}
