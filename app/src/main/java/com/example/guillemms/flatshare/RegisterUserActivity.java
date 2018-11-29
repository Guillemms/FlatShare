package com.example.guillemms.flatshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterUserActivity extends AppCompatActivity {

    private EditText name_edit;
    private EditText email_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        name_edit = findViewById(R.id.name_edit);
        email_edit = findViewById(R.id.email_edit);

        Button entrar_button = findViewById(R.id.enter_button);
        entrar_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_edit.getText().toString();
                String email = email_edit.getText().toString();

                // Firebase

                Intent intent = new Intent(this, FlatActivity.class);
                startActivity(intent);
            }
        });
    }
}
