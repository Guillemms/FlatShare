package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DebtActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    ArrayList<UserDebt> userDebts = new ArrayList<>();
    private String flatId;
    private String userId;
    private Map userNames;
    private DecimalFormat df = new DecimalFormat("#.##");

    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt);

        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        flatId = prefs.getString("flatId", "");
        userId = prefs.getString("userId", "");

        RecyclerView list = findViewById(R.id.debts_recyclerView);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        list.setAdapter(adapter);

        userNames = new HashMap<>();
        final Map<String, Float> usersDebtAmount = new HashMap<>();
        db.collection("Users")
                .whereEqualTo("ID Flat", flatId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = (String)document.getData().get("Name");
                                userNames.put(document.getId(), name);
                                usersDebtAmount.put(document.getId(), 0f);
                            }

                            db.collection("Flats")
                                    .document(flatId)
                                    .collection("ShoppingItem")
                                    .whereEqualTo("Buy", true)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                float total = 0f;
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Log.d("", document.getId() + " => " + document.getData());

                                                    String itemUserId = document.getString("ID User");
                                                    String itemPriceStr = document.getString("Price");
                                                    float itemPrice = Float.valueOf(itemPriceStr);
                                                    total += itemPrice;
                                                    usersDebtAmount.put(itemUserId, usersDebtAmount.get(itemUserId) + itemPrice);
                                                }

                                                int numUsers = userNames.size();
                                                float val = total/numUsers;

                                                for (Map.Entry<String, Float> entry : usersDebtAmount.entrySet()) {
                                                    String key = entry.getKey();
                                                    Float value = entry.getValue();
                                                    float userDebt = value - val;
                                                    userDebts.add(new UserDebt((String) userNames.get(key), df.format(userDebt)+"€"));
                                                }
                                                adapter.notifyItemRangeInserted(0, userDebts.size());
                                            } else {
                                                Log.d("", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });

                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }

                    }
                });

        Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = userDebts.size();
                userDebts.clear();
                adapter.notifyItemRangeRemoved(0, num);

                db.collection("Flats")
                        .document(flatId)
                        .collection("ShoppingItem")
                        .whereEqualTo("Buy", true)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String itemId = document.getId();

                                        db.collection("Flats")
                                                .document(flatId)
                                                .collection("ShoppingItem")
                                                .document(itemId)
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("", "DocumentSnapshot successfully deleted!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("", "Error deleting document", e);
                                                    }
                                                });
                                    }

                                    setResult(RESULT_OK);
                                    returnShopping();
                                    finish();

                                } else {
                                    Log.d("test", "Error getting documents: ", task.getException());
                                }

                            }
                        });

            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView userView;
        TextView debtView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.userView = itemView.findViewById(R.id.user_textView);
            this.debtView = itemView.findViewById(R.id.debt_textView);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder>{
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = getLayoutInflater()
                    .inflate(R.layout.layout_user_debt, viewGroup, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            UserDebt userDebt = userDebts.get(i);
            viewHolder.userView.setText(userDebt.getUserName());
            viewHolder.debtView.setText(userDebt.getUserDebt());
        }

        @Override
        public int getItemCount() {
            return userDebts.size();
        }
    }

    private void returnShopping(){
        Intent ShoppingIntent = new Intent(this, ShoppingListActivity.class);
        startActivity(ShoppingIntent);
    }
}
