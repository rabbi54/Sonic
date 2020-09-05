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
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.whatsappclone.FinalVariables;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Utils.ActivityUIUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private ImageButton phoneLoginButton;
    private EditText userEmail, userPassword;
    private TextView needNewAccountLink, forgetPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private ActivityUIUtils uiUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeField();
        initializeListener();
    }

    private void initializeField() {
        loginButton = findViewById(R.id.login_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        needNewAccountLink = findViewById(R.id.login_need_new_account);
        forgetPasswordLink = findViewById(R.id.login_forget_password_link);


//        Set progressbar for creating account
        loadingBar = new ProgressDialog(this, R.style.MyAlertDialogStyle);



//        Initialize firebase authentication
        mAuth = FirebaseAuth.getInstance();

//        ui utils
        uiUtils = new ActivityUIUtils(this);

    }

    private void initializeListener() {
        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user need a new account
                // direct the user to register activity
                sendUserToRegisterActivity();

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginToAccount();
            }
        });


        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              sendUserToPhoneLoginActivity();
            }
        });
    }



    private void loginToAccount() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();
        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            uiUtils.showToast("Please provide your email and password!");
        }
        else if(TextUtils.isEmpty(email)){
            uiUtils.showToast("Please provide your email!");
        }
        else if (TextUtils.isEmpty(password)) {
            uiUtils.showToast("Please enter your password!");
        }
        else{
            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait, you are about to login...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                if(mAuth.getCurrentUser().isEmailVerified()){
                                    loadingBar.dismiss();

                                    // If new user direct to settings activity
                                    // If old user direct to main activity
                                    sendingUserToActivity(mAuth.getCurrentUser(), FirebaseDatabase.getInstance().getReference(
                                            FinalVariables.FIREBASE_USER_USERS_REF
                                    ));
                                }
                                else{
                                    uiUtils.showToast("Please verify your email");
                                    loadingBar.dismiss();
                                }
                            }else {
                                uiUtils.showToast("Error : " + task.getException().getMessage());
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }


    // If user don't have any name and status then he is a new user so
    // Check that
    // If new user then, user must provide his username and status so redirect him to settings activity
    // If not new user the redirect to main activity.
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
                    sendUserToSettingsActivityForNewUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendUserToSettingsActivityForNewUser() {
        Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToMainActivity() {

        // User should not go back to Login activity if he is authenticated. So use the flag.
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void sendUserToPhoneLoginActivity() {
        Intent intent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        startActivity(intent);
    }

}