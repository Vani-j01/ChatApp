package com.example.letschat;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link register#newInstance} factory method to
 * create an instance of this fragment.
 */
public class register extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button reg_btn;
    private EditText User_email, User_password, User_name, User_Username;

    private FirebaseAuth mAuth;
    private DatabaseReference Rootrefference;
    private ProgressDialog progressBar;

    public register() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment register.
     */
    // TODO: Rename and change types and number of parameters
    public static register newInstance(String param1, String param2) {
        register fragment = new register();
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
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        reg_btn = view.findViewById(R.id.reg_btn);
        User_email = view.findViewById(R.id.email_reg_enter);
        User_password = view.findViewById(R.id.password_reg_enter);
        User_name = view.findViewById(R.id.name_enter);
        User_Username = view.findViewById(R.id.user_name_enter);

        mAuth = FirebaseAuth.getInstance();
        Rootrefference = FirebaseDatabase.getInstance().getReference();

        progressBar = new ProgressDialog(getActivity());

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
        return view;
    }

    private void CreateNewAccount() {

        String UserName = User_name.getText().toString();
        String UserUsername = User_Username.getText().toString();
        String UserEmail = User_email.getText().toString();
        String UserPassword = User_password.getText().toString();

        if (TextUtils.isEmpty(UserName)){
            Toast.makeText(getActivity(), "Please Enter Name..", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(UserUsername)){
            Toast.makeText(getActivity(), "Please Enter Username...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(UserEmail)) {
            Toast.makeText(getActivity(), "Please Enter Email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(UserPassword)) {
            Toast.makeText(getActivity(), "Please Enter Password...", Toast.LENGTH_SHORT).show();
            return;
        } else {
            progressBar.setTitle("Creating New Account");
            progressBar.setMessage("Please wait, while we create your Account");
            progressBar.setCanceledOnTouchOutside(true);
            progressBar.show();

            final HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("name", UserName);
            profileMap.put("email", UserEmail);
            profileMap.put("password", UserPassword);
            profileMap.put("username", UserUsername);

            mAuth.createUserWithEmailAndPassword(UserEmail, UserPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                Rootrefference.child("Users").child(currentUserID).setValue(profileMap);

                                Toast.makeText(getActivity(), "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                progressBar.dismiss();

                                ((LoginActivity)getActivity()).SendUserToMainActivity();
                            }
                            else
                                {
                                String error_msg = task.getException().toString();
                                Toast.makeText(getActivity(), "ERROR: " + error_msg, Toast.LENGTH_SHORT).show();
                                progressBar.dismiss();
                            }
                        }
                    });
        }
    }
}