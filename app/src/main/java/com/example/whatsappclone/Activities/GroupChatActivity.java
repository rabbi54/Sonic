package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.example.whatsappclone.FinalVariables;
import com.example.whatsappclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private ScrollView groupChatScrollView;
    private ImageButton imageButton;
    private EditText editText;
    private TextView displayTextMessages;
    private String currentGroupName;
    private String currentUserID;
    private String currentUserName;
    private DatabaseReference databaseReference;
    private DatabaseReference groupNameRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get(FinalVariables.FROM_GROUPFRAGMENT_TO_GROUPACTIVITY_GROUP_NAME).toString();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference(FinalVariables.FIREBASE_USER_USERS_REF);
        groupNameRef = FirebaseDatabase.getInstance().getReference(FinalVariables.FIREBASE_GROUPS_REF).child(currentGroupName);

        initializeFields();
        initializeListener();
        getUserInfo();

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    displayMessagesToTextView(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    displayMessagesToTextView(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void initializeFields() {
        groupChatScrollView = findViewById(R.id.group_chat_scroll_view);
        groupChatScrollView.setSmoothScrollingEnabled(true);
        Toolbar toolbar = findViewById(R.id.group_chat_app_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);
        imageButton = findViewById(R.id.group_send_message_button);
        editText = findViewById(R.id.group_message_input);
        displayTextMessages = findViewById(R.id.group_chat_text_display);

    }

    private void initializeListener() {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToDatabase();
                groupChatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                editText.setText("");
            }
        });
    }

    private void displayMessagesToTextView(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
        while(iterator.hasNext()){
//            String groupCreator = (String)((DataSnapshot)iterator.next()).getValue();
            String chatDate = (String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String)((DataSnapshot)iterator.next()).getValue();
            String chatName = (String)((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String)((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + ":\n" + chatMessage +"\n"
                    + chatTime + "\t\t" + chatDate + "\n\n\n");
            groupChatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }


    private void sendMessageToDatabase() {
        String message = editText.getText().toString().trim();
        String messageKey = groupNameRef.push().getKey();
        if(TextUtils.isEmpty(message)){

        }
        else{
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
            String currentDate = dateFormat.format(calendar.getTime());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            String currentTime = timeFormat.format(calendar.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);
            DatabaseReference groupMessageKeyRef = groupNameRef.child(messageKey);
            HashMap<String, Object> messageInfo = new HashMap<>();
                messageInfo.put(FinalVariables.FIREBASE_USER_USERNAME, currentUserName);
                messageInfo.put(FinalVariables.MESSAGE_CONTENT, message);
                messageInfo.put(FinalVariables.MESSAGE_DATE, currentDate);
                messageInfo.put(FinalVariables.MESSAGE_TIME, currentTime);

            groupMessageKeyRef.updateChildren(messageInfo);
            groupChatScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }


    private void getUserInfo() {
        databaseReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName = snapshot.child(FinalVariables.FIREBASE_USER_USERNAME).getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}