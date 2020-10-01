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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class login extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button log_btn, phone_log_btn;
    private EditText User_email, User_password;
    private ProgressDialog progressBar;
     private FirebaseAuth mAuth;
     private DatabaseReference RootReference;

    public login() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment login.
     */
    // TODO: Rename and change types and number of parameters
    public static login newInstance(String param1, String param2) {
        login fragment = new login();
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        log_btn = view.findViewById(R.id.login_btn);
        phone_log_btn = view.findViewById(R.id.phone_login_btn);
        User_email = view.findViewById(R.id.email_enter);
        User_password = view.findViewById(R.id.password_enter);

        mAuth = FirebaseAuth.getInstance();
        RootReference = FirebaseDatabase.getInstance().getReference();
        progressBar = new ProgressDialog(getActivity());

        log_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        

        return view;
    }

    private void AllowUserToLogin() {
        String email = User_email.getText().toString();
        String password = User_password.getText().toString();

        //Checking if user have enter all the fields
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Please Enter Email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Please Enter Password...", Toast.LENGTH_SHORT).show();
        } else {
            //Dialog Box informing user that login is going on
            progressBar.setTitle("Logging In");
            progressBar.setMessage("Please wait, while we LogIn to your Account");
            progressBar.setCanceledOnTouchOutside(true);
            progressBar.show();

            //logging in
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                ((LoginActivity) getActivity()).SendUserToMainActivity();
                                Toast.makeText(getActivity(), "Logged In Successfully", Toast.LENGTH_SHORT).show();
                                progressBar.dismiss();

                            } else {
                                String error_msg = task.getException().toString();
                                Toast.makeText(getActivity(), "ERROR: " + error_msg, Toast.LENGTH_SHORT).show();
                                progressBar.dismiss();
                            }
                        }
                    });
        }
    }


}