package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.whatsappclone.FinalVariables;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Utils.ActivityUIUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private ActivityUIUtils uiUtils;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeFields();
        initializeListener();

    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveUserInfo();
    }

    // This will retrieve user previous username , photo and status

    private void retrieveUserInfo(){
        databaseReference.child(FinalVariables.FIREBASE_USER_USERS_REF)
                .child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.hasChild(FinalVariables.FIREBASE_USER_USERNAME)
                        && snapshot.hasChild(FinalVariables.FIREBASE_USER_PROFILE_PHOTO)){
                            String retrieveUserName = snapshot.child(FinalVariables.FIREBASE_USER_USERNAME)
                                    .getValue().toString();
                            String retrieveUserStatus = snapshot.child(FinalVariables.FIREBASE_USER_STATUS)
                                    .getValue().toString();
                            String retrieveUserProfileImageURL = snapshot.child(FinalVariables.FIREBASE_USER_PROFILE_PHOTO)
                                    .getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
//                            userProfileImage.setImageURI();

                        }else if(snapshot.exists() && snapshot.hasChild(FinalVariables.FIREBASE_USER_USERNAME)){
                            String retrieveUserName = snapshot.child(FinalVariables.FIREBASE_USER_USERNAME)
                                    .getValue().toString();
                            String retrieveUserStatus = snapshot.child(FinalVariables.FIREBASE_USER_STATUS)
                                    .getValue().toString();
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initializeListener() {
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });
    }



    private void initializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_user_status);
        userProfileImage = findViewById(R.id.set_profile_image);


        //utils
        uiUtils = new ActivityUIUtils(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

    }

    private void updateSettings() {
        final String setUserName = userName.getText().toString().trim();
        String setUserStatus = userStatus.getText().toString().trim();

        if(TextUtils.isEmpty(setUserName)){
            uiUtils.showToast("Please write your username");
        } else if (TextUtils.isEmpty(setUserStatus)) {

            uiUtils.showToast("Please write your status");
        }
        else{
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put(FinalVariables.FIREBASE_USER_ID, currentUserID);
            profileMap.put(FinalVariables.FIREBASE_USER_USERNAME, setUserName);
            profileMap.put(FinalVariables.FIREBASE_USER_STATUS, setUserStatus);
            databaseReference.child(FinalVariables.FIREBASE_USER_USERS_REF)
                    .child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                uiUtils.showToast("Profile updated successfully. Welcome " + setUserName);
                                sendUserToSMainActivityForNewUser();
                            }
                            else{
                                uiUtils.showToast("Profile Update Error: "+task.getException().getMessage());
                            }
                        }
                    });
        }


    }

    private void sendUserToSMainActivityForNewUser() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);

        startActivity(intent);
        finish();
    }
}