package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ShoppingItemActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText nameItem;
    private EditText detailItem;
    private EditText priceItem;
    private Button saveButton;
    private CheckBox buy;
    private String itemId;
    private String flatId;
    private String userId;
    private String lastPrice;
    private Boolean exi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_item);

        nameItem = findViewById(R.id.nomItem);
        detailItem = findViewById(R.id.detallItem);
        priceItem = findViewById(R.id.preuItem);
        saveButton = findViewById(R.id.btn_save);
        buy = findViewById(R.id.itemComprat);

        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        flatId = prefs.getString("flatId", "");
        userId = prefs.getString("userId", "");

        Intent intent = getIntent();
        if(intent!=null){
            itemId = intent.getStringExtra("itemId");
            if(itemId!=null){
                exi = true;
                db.collection("Flats")
                    .document(flatId)
                    .collection("ShoppingItem")
                    .document(itemId)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("Name");
                                nameItem.setText(name);
                                String detail = document.getString("Detail");
                                detailItem.setText(detail);
                                lastPrice = document.getString("Price");
                                priceItem.setText(lastPrice);
                                boolean isbuy = document.getBoolean("Buy");
                                if(isbuy==true){
                                    buy.setChecked(true);
                                } else{
                                    buy.setChecked(false);
                                }
                            } else {
                                Log.d("item", "No such document");
                            }
                        } else {
                            Log.d("item", "get failed with ", task.getException());
                        }
                    }
                });


            } else{
                exi = false;
            }
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = nameItem.getText().toString();
                String itemDetail = detailItem.getText().toString();
                final String itemPrice = priceItem.getText().toString();

                Map<String, Object> camps = new HashMap<>();
                camps.put("Name", itemName);
                camps.put("Detail", itemDetail);
                if(buy.isChecked()) {
                    camps.put("Buy", true);
                    camps.put("ID User", userId);
                    camps.put("Price", itemPrice);
                } else{
                    camps.put("Buy", false);
                    camps.put("ID User", "");
                    camps.put("Price", "");
                }

                if(exi==true){
                    db.collection("Flats")
                            .document(flatId)
                            .collection("ShoppingItem")
                            .document(itemId)
                            .set(camps)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("", "DocumentSnapshot successfully written!");
                                    setResult(RESULT_OK);
                                    finish();
                                    openShoppingListActivity();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("", "Error writing document", e);
                                }
                            });
                } else {
                    db.collection("Flats")
                            .document(flatId)
                            .collection("ShoppingItem")
                            .add(camps)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("", "DocumentSnapshot written with ID: " + documentReference.getId());
                                    setResult(RESULT_OK);
                                    finish();
                                    openShoppingListActivity();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("", "Error adding document", e);
                                }
                            });;
                }
            }
        });
    }

    private void openShoppingListActivity(){
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(itemId != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.new_item, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                db.collection("Flats")
                        .document(flatId)
                        .collection("ShoppingItem")
                        .document(itemId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("test", "DocumentSnapshot successfully deleted!");
                                setResult(RESULT_OK);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("test", "Error deleting document", e);
                                setResult(RESULT_CANCELED);
                            }
                        });
                break;
        }
        return true;
    }
}
