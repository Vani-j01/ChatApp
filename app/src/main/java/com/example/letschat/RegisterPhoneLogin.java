package com.example.letschat;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterPhoneLogin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterPhoneLogin extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText userName, userPassword, userUsername;
    private Button regbtn;
    private String phoneNumber;

    private FirebaseAuth mAuth;
    private DatabaseReference Rootrefference;
    private ProgressDialog progressBar;

    public RegisterPhoneLogin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterPhoneLogin.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterPhoneLogin newInstance(String param1, String param2) {
        RegisterPhoneLogin fragment = new RegisterPhoneLogin();
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
        View view = inflater.inflate(R.layout.fragment_register_phone_login, container, false);

        //Initializations
        userName = view.findViewById(R.id.phuser_name_enter);
        userUsername = view.findViewById(R.id.phuser_user_name_enter);
        userPassword = view.findViewById(R.id.phuser_password_enter);

        regbtn = view.findViewById(R.id.phuser_reg_btn);

        mAuth = FirebaseAuth.getInstance();
        Rootrefference = FirebaseDatabase.getInstance().getReference();

        progressBar = new ProgressDialog(getActivity());

        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddUserInfo();
            }
        });

        return view;
    }

    private void AddUserInfo() {
        final String UserName = userName.getText().toString();
        final String UserUsername = userUsername.getText().toString();
        final String UserPassword = userPassword.getText().toString();


        //Checking If user filled all the fields
        if (TextUtils.isEmpty(UserName)) {
            Toast.makeText(getActivity(), "Please Enter Name..", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(UserUsername)) {
            Toast.makeText(getActivity(), "Please Enter Username...", Toast.LENGTH_SHORT).show();
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


            String currentUserID = mAuth.getCurrentUser().getUid();
            String num = mAuth.getCurrentUser().getPhoneNumber();
            Log.i("log", "AddUserInfo: "+num);
            final HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("name", UserName);
            profileMap.put("password", UserPassword);
            profileMap.put("username", UserUsername);
            profileMap.put("phonenumber",num);
            profileMap.put("uid", currentUserID);
            profileMap.put("status", "Available");

            Rootrefference.child("Users").child(currentUserID).setValue(profileMap);

            Toast.makeText(getActivity(), "Account Created Successfully", Toast.LENGTH_SHORT).show();

            progressBar.dismiss();

            ((LoginActivity) getActivity()).SendUserToMainActivity();

        }
    }
}