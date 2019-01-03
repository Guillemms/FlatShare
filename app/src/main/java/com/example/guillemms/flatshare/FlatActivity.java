package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class FlatActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView ID;
    private EditText name;
    private Button create;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flat);


        ID = findViewById(R.id.text_view_id);
        name = findViewById(R.id.editText);
        create = findViewById(R.id.button_create);

        Intent intent = getIntent();
        if(intent!=null){
            userId = intent.getStringExtra("userId");
            ID.setText(userId);
        }

        db.collection("Users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                String flatIde = documentSnapshot.getString("ID Flat");
                if(flatIde!=null){
                    SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
                    prefs.edit().putString("flatId", flatIde).commit();
                    finish();
                    openTaskActivity();
                }
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String flatName = name.getText().toString();

                Map<String, Object> camps = new HashMap<>();
                camps.put("Name", flatName);

                db.collection("Flats").add(camps).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String flatId = documentReference.getId();

                        db.collection("Users").document(userId).update("ID Flat", flatId);

                        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
                        prefs.edit().putString("flatId", flatId).commit();

                        Toast.makeText(FlatActivity.this, "Pis creat", Toast.LENGTH_SHORT).show();
                        finish();
                        openTaskActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FlatActivity.this, "Error al crear el pis", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }

    private void openTaskActivity() {
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
    }
}
