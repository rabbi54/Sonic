package com.example.whatsappclone.Utils;

import android.content.Context;
import android.widget.Toast;

public class ActivityUIUtils {
    Context context;
    public ActivityUIUtils(Context context) {
        this.context = context;
    }
    public void showToast(String message){
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    }
}
