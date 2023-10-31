package com.example.langsync.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AuthenticationUtilities {
    private final Context context;
    public AuthenticationUtilities(Context context) {
        this.context = context;
    }

    // ChatGPT usage: No
    public void navigateTo(Class<?> cls, String message){
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, cls);
                context.startActivity(intent);
            }
        });
    }

    // ChatGPT usage: No
    public void showToast(String message){
        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }
}
