package com.example.cookbookmotion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigActivity extends WearableActivity {

    private Button login;
    private EditText userTextView;
    private EditText pwTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Enables Always-on
        setAmbientEnabled();

        login = (Button) findViewById(R.id.loginBtn);
        userTextView = (EditText) findViewById(R.id.nameInput);
        pwTextView = (EditText) findViewById(R.id.pwInput);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                sp.edit().putString("username", userTextView.getText().toString()).apply();
                sp.edit().putString("password", pwTextView.getText().toString()).apply();

                goToMainActivity();
            }
        });

    }


    private void goToMainActivity(){
        finish();
    }
}
