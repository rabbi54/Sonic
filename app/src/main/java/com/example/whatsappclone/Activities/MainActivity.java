package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.whatsappclone.Adapters.TabsAccessorAdapter;
import com.example.whatsappclone.FinalVariables;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Utils.ActivityUIUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private AppBarLayout mAppBarLayout;
    private ViewPager mViewPager;
    private TabsAccessorAdapter mTabsAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ActivityUIUtils uiUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       initializeFields();


    }

    private void initializeFields() {

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.app_name);


        mViewPager = findViewById(R.id.main_page_tabs_pager);
        mTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccessorAdapter);


        mTabLayout = findViewById(R.id.main_page_tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);

        //Firebase fields
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.getIdToken(true);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference(FinalVariables.FIREBASE_USER_USERS_REF);

        //utils
        uiUtils = new ActivityUIUtils(this);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            // user is not authenticated!
            // create an intent to authenticate the user
            sendUserToLoginActivity();
        } else if (!currentUser.isEmailVerified() && currentUser.getProviderId().equals(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
            sendUserToLoginActivity();
        }

    }
    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_option){
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.main_settings_option){
            sendUserToSettingsActivity();
        }
        if(item.getItemId() == R.id.main_find_friends_option){

        }
        if(item.getItemId() == R.id.main_create_group_option){
            requestForNewGroup();
        }
        return true;
    }

    private void requestForNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
        builder.setTitle("Enter Group Name : ");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Fun Zone");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString().trim();
                if(TextUtils.isEmpty(groupName)){
                    uiUtils.showToast("Please write the group name. ");
                }else{
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void createNewGroup(final String groupName) {
        databaseReference.child(FinalVariables.FIREBASE_GROUPS_REF).child(groupName)
        .setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    databaseReference.child(FinalVariables.FIREBASE_GROUPS_REF).child(groupName).child(FinalVariables
                            .FIREBASE_GROUPS_GROUP_CREATOR).setValue(currentUser.getUid())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        uiUtils.showToast("Group has been created successfully!");
                                    }
                                    else{
                                        uiUtils.showToast("Group Creation Error: " + task.getException()
                                        .getMessage());
                                    }
                                }
                            });
                }
                else{
                    uiUtils.showToast("Group Creation Error: " + task.getException()
                            .getMessage());
                }
            }
        });

    }


}