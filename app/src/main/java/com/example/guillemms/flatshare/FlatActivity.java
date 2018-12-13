package com.example.guillemms.flatshare;

import android.content.Intent;
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

    //TODO: Mostrar id usuari
    //TODO: Revisar constantment si l'usuari esta en un pis
    //TODO: Preguntar per fer una array de ID d'usuaris + colección en fairebase

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
        }
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String flatName = name.getText().toString();

                Map<String, Object> camps = new HashMap<>();
                camps.put("Name", flatName);

                db.collection("Flats").add(camps).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(FlatActivity.this, "Pis creat", Toast.LENGTH_SHORT).show();
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

        db.collection("Users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
               // Si algú altre canvi el camp "pis" d'aquest usuari llavors
               // passem a la següent activitat.
                String flatId = documentSnapshot.getString("ID Flat");
                if(flatId!=null){
                    openTaskActivity();
                }
            }
        });
    }

    private void openTaskActivity() {
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
    }
}
