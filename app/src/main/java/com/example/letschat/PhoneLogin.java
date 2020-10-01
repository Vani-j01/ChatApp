package com.example.letschat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhoneLogin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhoneLogin extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

   private EditText phone_number_input, code_input;
   private Button send_code_btn, verify_btn;
   private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
   private String mVerificationId;
   private PhoneAuthProvider.ForceResendingToken mResendToken;
   private FirebaseAuth mAuth;
   private ProgressDialog loadingBar;

    public PhoneLogin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PhoneLogin.
     */
    // TODO: Rename and change types and number of parameters
    public static PhoneLogin newInstance(String param1, String param2) {
        PhoneLogin fragment = new PhoneLogin();
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
        View view = inflater.inflate(R.layout.fragment_phone_login, container, false);

        phone_number_input = view.findViewById(R.id.number_input);
        code_input = view.findViewById(R.id.verification_code_input);
        send_code_btn= view.findViewById(R.id.send_code_btn);
        verify_btn= view.findViewById(R.id.verify_code_btn);
        mAuth= FirebaseAuth.getInstance();
        loadingBar= new ProgressDialog(getActivity());

        buttonFunctions();

        return view;
    }

    private void buttonFunctions() {

        send_code_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = phone_number_input.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(getActivity(), "Please provide a phone number", Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait while we send the Code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            getActivity(),               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
            Toast.makeText(getActivity(), "Invalid Information Provided\n\n Remember to mention correct Country Code", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                loadingBar.dismiss();
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(getActivity(), "Code has been sent,Please Enter it below",Toast.LENGTH_SHORT).show();
                code_input.setVisibility(View.VISIBLE);
                verify_btn.setVisibility(View.VISIBLE);
            }
        };



        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verificationCode = code_input.getText().toString();

                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(getActivity(),"Please Enter Code",Toast.LENGTH_SHORT).show();
                }

                else {
                    loadingBar.setTitle("Verification Cose");
                    loadingBar.setMessage("Please wait while we Authenticate your Account");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(getActivity(),"Verification Successful", Toast.LENGTH_SHORT).show();

                            ((LoginActivity) getActivity()).openPhoneUserInfo();
                        }
                        else {
                            String errorMsg = task.getException().toString();
                            Toast.makeText(getActivity(),"Error: "+errorMsg,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}