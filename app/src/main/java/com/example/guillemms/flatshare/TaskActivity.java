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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {

    Map<String, Object> task = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    String flatId;

    ArrayList<Map<String, Object>> flatUsers = new ArrayList<>();

    Adapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Intent intent = getIntent();
        if(intent != null){
            
        }

        final EditText nameEdit = findViewById(R.id.name_editText);
        final EditText descriptionEdit = findViewById(R.id.description_editText);
        final EditText dateEdit = findViewById(R.id.date_editText);
        final Spinner spinner = findViewById(R.id.periodicity_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.periodicity_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                String description = descriptionEdit.getText().toString();
                String dateString =  dateEdit.getText().toString();
                int periodicity = spinner.getSelectedItemPosition();
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                try {
                    Date date = format.parse(dateString);
                    task.put("Name", name);
                    task.put("Description", description);
                    task.put("Initial date", date);
                    task.put("Periodicity", periodicity);

                    ArrayList<Object> userIds = new ArrayList<Object>();
                    for(Map<String, Object> user : flatUsers) {
                        userIds.add(user.get("id"));
                    }
                    task.put("User IDs", userIds);
                    saveTaskToFirestore();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        final SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        flatId = prefs.getString("flatId", "");

        db.collection("Users")
                .whereEqualTo("ID Flat", flatId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> user = new HashMap<>();
                                user.put("id", document.getId());
                                user.put("data", document.getData());
                                flatUsers.add(user);
                            }
                            onGetUsers();
                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }
                    }
                });

        RecyclerView userList = findViewById(R.id.users_recyclerView);
        userList.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new Adapter();
        userList.setAdapter(usersAdapter);
    }

    private void saveTaskToFirestore() {
        db.collection("Flats")
                .document(flatId)
                .collection("Tasks")
                .add(task)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("newTask", "DocumentSnapshot written with ID: " + documentReference.getId());
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("newTask", "Error adding document", e);
                        setResult(RESULT_CANCELED);
                    }
                });
    }

    private void onGetUsers() {
        usersAdapter.notifyItemRangeInserted(0, flatUsers.size());
    }

    // Recyclerview of users
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView positionView;
        TextView userView;
        Button upButton;
        Button downButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.positionView = itemView.findViewById(R.id.position_textView);
            this.userView = itemView.findViewById(R.id.user_textView);
            this.upButton = itemView.findViewById(R.id.up_button);
            this.downButton = itemView.findViewById(R.id.down_button);

            upButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemButtonClicked(true, getAdapterPosition());
                }
            });

            downButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemButtonClicked(false, getAdapterPosition());
                }
            });
        }
    }

    void onItemButtonClicked(boolean isUp, int pos) {
        int nextPos = isUp? pos-1 : pos+1;
        boolean noChange = nextPos >= flatUsers.size() || nextPos < 0;
        if(!noChange) {
            Collections.swap(flatUsers, pos, nextPos);
            usersAdapter.notifyItemMoved(pos, nextPos);
            //usersAdapter.notifyItemChanged(pos);
            //usersAdapter.notifyItemChanged(nextPos);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = getLayoutInflater()
                    .inflate(R.layout.layout_user, viewGroup, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            Map user = (Map) flatUsers.get(i).get("data");
            String userName = (String) user.get("Name");
            viewHolder.userView.setText(userName);
            viewHolder.positionView.setText(String.valueOf(i));
        }

        @Override
        public int getItemCount() {
            return flatUsers.size();
        }
    }

}
