package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {

    private static final int EDIT_ITEM = 0;
    private static final int RESOLVE_DEBT = 1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Map> items;
    private RecyclerView itemsRV;
    private String flatId;
    private String userId;
    private Button goTask;
    private Adapter adapter = new Adapter();
    private Map userNames;

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
        goTask = findViewById(R.id.task_btn);

        goTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTaskActivity();
            }
        });

        userNames = new HashMap<>();
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = (String)document.getData().get("Name");
                                userNames.put(document.getId(), name);
                            }

                            getShoppingItems();
                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }

                    }
                });

        itemsRV.setAdapter(adapter);

        Button debtsButton = findViewById(R.id.debts_button);
        debtsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDebtButtonClick();
            }
        });
    }

    private void onDebtButtonClick() {
        Intent intent = new Intent(this, DebtActivity.class);
        startActivityForResult(intent, RESOLVE_DEBT);
    }

    private void getShoppingItems() {
        int num = items.size();
        items.clear();
        adapter.notifyItemRangeRemoved(0, num);

        db.collection("Flats").document(flatId).collection("ShoppingItem")
                .orderBy("Buy")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map flatItemAll = new HashMap<>();
                                flatItemAll.put("id", document.getId());
                                flatItemAll.put("data", document.getData());
                                items.add(flatItemAll);
                                adapter.notifyItemInserted(items.size()-1);
                            }
                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }
                    }
                });

        adapter.notifyItemRangeInserted(0, items.size());
    }

    private void OnlyThisUser(){
        int num = items.size();
        items.clear();
        adapter.notifyItemRangeRemoved(0, num);

        db.collection("Flats")
            .document(flatId)
            .collection("ShoppingItem")
            .whereEqualTo("ID User", userId)
            .orderBy("Buy")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map flatItemAll = new HashMap<>();
                    flatItemAll.put("id", document.getId());
                    flatItemAll.put("data", document.getData());

                    items.add(flatItemAll);
                }
            } else {
                Log.d("test", "Error getting documents: ", task.getException());
            }
            }
        });

        adapter.notifyItemRangeInserted(0, items.size());
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView userView;
        private TextView itemNameView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.userView = itemView.findViewById(R.id.nameUserBuy);
            this.itemNameView = itemView.findViewById(R.id.nameitem);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openItemActivity(getAdapterPosition());
                }
            });
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
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            Map item = items.get(position);
            Map itemData = (Map) item.get("data");

            String itemName = itemData.get("Name").toString();
            String userBID = itemData.get("ID User").toString();
            String userName = (String)userNames.get(userBID);
            boolean isBought = (boolean)itemData.get("Buy");

            holder.itemNameView.setText(itemName);
            if(isBought) {
                holder.itemNameView.setPaintFlags(holder.itemNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.userView.setText(userName);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

    public void openItemActivity(int pos){
        String itemId = (String) items.get(pos).get("id");
        Intent ItemIntent = new Intent(this, ShoppingItemActivity.class);
        ItemIntent.putExtra("itemId", itemId);
        startActivityForResult(ItemIntent, EDIT_ITEM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case EDIT_ITEM:
                getShoppingItems();
                break;
            case RESOLVE_DEBT:
                getShoppingItems();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openTaskActivity() {
        Intent TaskIntent = new Intent(this, TaskListActivity.class);
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
            case R.id.menu_newUser:
                Intent newUserIntent = new Intent(this, AddTenantActivity.class);
                newUserIntent.putExtra("lastActivity", 1);
                startActivity(newUserIntent);
                break;
        }
        return true;
    }
}
