package com.example.guillemms.flatshare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {

    //Conectes amb la Firebase, necessari per cada activitat
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView hola;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

       /*Forma de obtenir la info de Firebase
        db.collection("pisos").document("WExoO2OJvJGWPlAmBZLd").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                String adresa = documentSnapshot.getString("adreça");
                //hola.setText(adresa);
            }
        });

        Forma de introduir dades a Firebase
        Map<String, Object> camps = new HashMap<>();
        camps.put("adreça", "Colom 1");
        camps.put("numincquilins", 4);

        db.collection("pisos").add(camps).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(ShoppingListActivity.this, "Pis gravat", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShoppingListActivity.this, "Fallo al gravar pis", Toast.LENGTH_SHORT).show();
            }
        });*/




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shopping_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_newItem:
                //Afaguir nou item
                break;
            case R.id.menu_filterItem:
                //Filtrar els teus items comprats
                break;
            case R.id.menu_newUser:
                Intent intent = new Intent(this, AddTenantActivity.class);
                intent.putExtra("lastActivity", 1);
                startActivity(intent);
                break;
        }
        return true;
    }
}
