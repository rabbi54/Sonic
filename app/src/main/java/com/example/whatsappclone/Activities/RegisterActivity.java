package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappclone.FinalVariables;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Utils.ActivityUIUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView alreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private ActivityUIUtils uiUtils;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeField();
        initializeListener();
    }

    private void initializeField() {
        createAccountButton  = findViewById(R.id.register_button);
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        alreadyHaveAccountLink = findViewById(R.id.register_already_have_account_link);


//        Set progressbar for creating account
        loadingBar = new ProgressDialog(this, R.style.MyAlertDialogStyle);



//        Initialize firebase authentication, firebase database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

//        UI utils
        uiUtils = new ActivityUIUtils(this);

    }

    private void initializeListener() {
        alreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();
        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            uiUtils.showToast("Please provide your email and password!");
        }
        else if(TextUtils.isEmpty(email)){
            uiUtils.showToast("Please provide your email!");
        } else if (TextUtils.isEmpty(password)) {
            uiUtils.showToast("Please enter your password!");
        }
        else{
            loadingBar.setTitle("Creating new Account ");
            loadingBar.setMessage("Please wait, your account is being created...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

                                                    // Save userID to firebase realtime database
                                                    String currentUserID = mAuth.getCurrentUser().getUid();
                                                    databaseReference.child(FinalVariables.FIREBASE_USER_USERS_REF).child(currentUserID).setValue("");


                                                    uiUtils.showToast("Verification email sent to " + mAuth.getCurrentUser().getEmail() + ". Please check your email for verification.");
                                                    loadingBar.dismiss();
                                                    sendUserToLoginActivity();
                                                }else{
                                                    uiUtils.showToast("Failed to send email : " + task.getException().getMessage());
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });

                            }
                        }
                    });
        }

    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}