package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Map> items;
    private RecyclerView itemsRV;
    private String flatId;
    private String userId;
    private String userName;
    private TextView userNameDebt;
    private TextView userDebt;
    private Button goTask;
    private Adapter adapter = new Adapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        flatId = prefs.getString("flatId", "");
        userId = prefs.getString("userId", "");

        items = new ArrayList<>();
        itemsRV = findViewById(R.id.RV_Item);
        itemsRV.setLayoutManager(new LinearLayoutManager(this));
        userNameDebt = findViewById(R.id.user_name);
        userDebt = findViewById(R.id.user_debt);
        goTask = findViewById(R.id.task_btn);

        userNameDebt.setText(userId);

        goTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTaskActivity();
            }
        });

       /* db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map user = task.getResult().getData();
                    String name = user.get("Name").toString();
                    userNameDebt.setText(name);
                    String debt = user.get("Debt").toString();
                    userDebt.setText(debt);
                } else {
                    Log.d("test", "Error getting documents: ", task.getException());
                }
            }
        });*/

        itemsRV.setAdapter(adapter);

        db.collection("Flats").document(flatId).collection("ShoppingItem")
                .orderBy("Buy").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map flatItem = document.getData();
                        items.add(flatItem);
                        adapter.notifyItemInserted(items.size()-1);
                    }
                } else {
                    Log.d("test", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void OnlyThisUser(){
        items = new ArrayList<>();
        db.collection("Flats").document(flatId).collection("ShoppingItem").whereArrayContains("ID User", userId)
                .orderBy("Buy").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map flatItem = document.getData();
                        items.add(flatItem);
                        adapter.notifyItemInserted(items.size()-1);
                    }
                } else {
                    Log.d("test", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView userView;
        private TextView itemNameView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userView = itemView.findViewById(R.id.nameUserBuy);
            itemNameView = itemView.findViewById(R.id.nameitem);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater()
                    .inflate(R.layout.layout_shopping, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map item = items.get(position);

            String itemName = item.get("Name").toString();
            String userBID = item.get("ID User").toString();
            db.collection("Users").document(userBID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Map userb = task.getResult().getData();
                    userName = userb.get("Name").toString();
                }
            });

            holder.itemNameView.setText(itemName);
            holder.userView.setText(userName);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public void openDebtActivity(View v){
        Intent DebtIntent = new Intent(this, DebtActivity.class);
        startActivity(DebtIntent);
    }

    public void openItemActivity(View v){
        Intent ItemIntent = new Intent(this, DebtActivity.class);
        //ItemIntent.putExtra("itemId", itemId);
        startActivity(ItemIntent);
    }

    private void openTaskActivity() {
        Intent TaskIntent = new Intent(this, ShoppingListActivity.class);
        startActivity(TaskIntent);
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
                Intent newItemIntent = new Intent(this, ShoppingItemActivity.class);
                startActivity(newItemIntent);
                break;
            case R.id.menu_filterItem:
                OnlyThisUser();
                break;
            case R.id.menu_newUser:
                Intent newUserIntent = new Intent(this, AddTenantActivity.class);
                newUserIntent.putExtra("lastActivity", 1);
                startActivity(newUserIntent);
                break;
        }
        return true;
    }
}
