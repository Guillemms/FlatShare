package com.example.guillemms.flatshare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterUserActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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

                Map<String, Object> camps = new HashMap<>();
                camps.put("Name", name);
                camps.put("Email", email);

                db.collection("Users").add(camps).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(RegisterUserActivity.this, "Registrat", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterUserActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                    }
                });

                activity();
            }
        });
    }

    private void activity(){
        Intent intent = new Intent(this, FlatActivity.class);
        startActivity(intent);
    }
}
