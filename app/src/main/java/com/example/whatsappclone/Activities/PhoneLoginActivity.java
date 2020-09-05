package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.whatsappclone.FinalVariables;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Utils.ActivityUIUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton, verifyCodeButton;
    private EditText inputPhoneNumber, inputVerificationCode;
    private TextView resendCode;
    private ActivityUIUtils uiUtils;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private boolean verificationInProgress = false;
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final String KEY_PHONE_NUMBER = "key_phone_number";
    CountryCodePicker ccp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
            Log.d("Error Phone", "Ans is" + verificationInProgress);
            if(verificationInProgress){

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                ccp.setVisibility(View.INVISIBLE);
                verifyCodeButton.setVisibility(View.VISIBLE);
                resendCode.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }
        }

        initializeFields();
        initializeListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (verificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification('+'+ccp.getSelectedCountryCode()+inputPhoneNumber.getText().toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress);
//        outState.putString(KEY_PHONE_NUMBER, inputPhoneNumber.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
//        inputPhoneNumber.setText(savedInstanceState.getString(KEY_PHONE_NUMBER));
    }


    private void initializeFields() {
        sendVerificationCodeButton = findViewById(R.id.send_verification_code_btn);
        verifyCodeButton = findViewById(R.id.verify_code_btn);
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerificationCode = findViewById(R.id.phone_number_verification_code_input);
        uiUtils = new ActivityUIUtils(this);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        resendCode = findViewById(R.id.resend_code);



        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                verificationInProgress = false;
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();
                //Invalid phone number
                uiUtils.showToast("Invalid phone number. Please enter phone number with your country code. ");
                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);
                ccp.setVisibility(View.VISIBLE);
                verifyCodeButton.setVisibility(View.INVISIBLE);
                resendCode.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
                Log.d("Error Phone find", e.getMessage().toString());
                Log.d("Error Phone no",inputPhoneNumber.getText().toString());
                verificationInProgress = false;


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("Code :", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                progressDialog.dismiss();
                uiUtils.showToast("Code has been sent, please check. ");
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                ccp.setVisibility(View.INVISIBLE);
                verifyCodeButton.setVisibility(View.VISIBLE);
                resendCode.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }

        };
    }

    private void initializeListener() {
        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String phone = inputPhoneNumber.getText().toString();
                if(validatePhoneNumber()){
                    String phoneNumber;
                    progressDialog.setTitle("Phone Number Verification");
                    progressDialog.setMessage("Please wait. We are verifying the phone number. ");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    phoneNumber = '+'+ccp.getSelectedCountryCode()+phone;
                    Log.d("Error PhoneCountryCode", ccp.getSelectedCountryCode());
                    Log.d("Error Phone Number", phoneNumber);
                    startPhoneNumberVerification(phoneNumber);

                }
            }
        });

        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                ccp.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verificationCode)){
                    uiUtils.showToast("Please provide the verification code.");
                }
                else{
                    progressDialog.setTitle("Code Verification");
                    progressDialog.setMessage("Please wait. We are verifying the code. ");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }
            }
        });

        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode('+'+ccp.getSelectedCountryCode()+inputPhoneNumber.getText().toString(), mResendToken);
            }
        });



//        Callback of phone number checking


    }

    private void sendingUserToActivity(FirebaseUser currentUser,
                                   DatabaseReference databaseReference) {
        String currentUserID = currentUser.getUid();
        databaseReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.child(FinalVariables.FIREBASE_USER_USERNAME).exists())){
//                    uiUtils.showToast("Welcome "+ snapshot.child(FinalVariables.FIREBASE_USER_USERNAME).getValue());
//                    loadingBar.dismiss();
                    uiUtils.showToast("You are logged in!");
                    sendUserToMainActivity();
                }
                else{
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = inputPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            uiUtils.showToast("Phone number is required");
            return false;
        }

        return true;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                callbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        verificationInProgress = true;
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                callbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("phoneSignIn", "signInWithCredential:success");
                            progressDialog.dismiss();
                            uiUtils.showToast("You are logged in.");
                            mAuth.getCurrentUser().getIdToken(true);
                            sendingUserToActivity(mAuth.getCurrentUser(), FirebaseDatabase.getInstance().getReference(
                                    FinalVariables.FIREBASE_USER_USERS_REF
                            ));

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("error phoneSignIn", "signInWithCredential:failure", task.getException());
                            uiUtils.showToast("Sign In Failed! Please try again. " );
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                progressDialog.dismiss();
                                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                                inputPhoneNumber.setVisibility(View.VISIBLE);
                                ccp.setVisibility(View.VISIBLE);
                                verifyCodeButton.setVisibility(View.INVISIBLE);
                                resendCode.setVisibility(View.INVISIBLE);
                                inputVerificationCode.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });
    }

    private void sendUserToSettingsActivity() {
        Intent intent = new Intent(PhoneLoginActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToMainActivity() {

        // User should not go back to Login activity if he is authenticated. So use the flag.
        Intent intent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}