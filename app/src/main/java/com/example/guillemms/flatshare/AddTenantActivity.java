package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddTenantActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText ID;
    private Button addT;
    private String flatId;
    private int lact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        ID = findViewById(R.id.id_edit);
        addT = findViewById(R.id.afegeix_btn);

        Intent intent = getIntent();
        if(intent!=null){
            lact = intent.getIntExtra("lastActivity", 0);
        }

        addT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String userId = ID.getText().toString();

                SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
                flatId = prefs.getString("FlatId", null);

                final DocumentReference docRef = db.collection("Users").document(userId);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                docRef.update("ID Flat", flatId);
                                //Afaguir id usuari a array de ids del pis
                                finish();
                                returnLastActivity(lact);
                            } else {
                                ID.setText("");
                                Toast.makeText(AddTenantActivity.this, "La ID introducida no existe", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

    }

    private void returnLastActivity(int lact){
        switch (lact){
            case 1: Intent intent = new Intent(this, ShoppingListActivity.class);
                    startActivity(intent);
                    break;
            case 2: Intent intent2 = new Intent(this, TaskListActivity.class);
                    startActivity(intent2);
                    break;
        }
    }
}
