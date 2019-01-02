package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

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
                db.collection("Flats").document(flatId)
                        .collection("ShoppingItem").document(itemId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        String name = documentSnapshot.getString("Name");
                        nameItem.setText(name);
                        String detail = documentSnapshot.getString("Detail");
                        detailItem.setText(detail);
                        String price = documentSnapshot.getString("Price");
                        priceItem.setText(price);
                        boolean isbuy = documentSnapshot.getBoolean("Buy");
                        if(isbuy==true){
                            buy.setChecked(true);
                        } else{
                            buy.setChecked(false);
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
                String itemPrice = priceItem.getText().toString();

                Map<String, Object> camps = new HashMap<>();
                camps.put("Name", itemName);
                camps.put("Detail", itemDetail);
                camps.put("Price", itemPrice);
                if(buy.isChecked()) {
                    camps.put("Buy", true);
                    camps.put("ID User", userId);
                } else{
                    camps.put("Buy", false);
                    camps.put("ID User", "");
                }

                if(exi==true){
                    db.collection("Flats").document(flatId)
                            .collection("ShoppingItem").document(itemId).set(camps);
                    finish();
                    openShoppingListActivity();
                } else {
                    db.collection("Flats").document(flatId)
                            .collection("ShoppingItem").add(camps);
                    finish();
                    openShoppingListActivity();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                //Borrar producte
                break;
        }
        return true;
    }
}
