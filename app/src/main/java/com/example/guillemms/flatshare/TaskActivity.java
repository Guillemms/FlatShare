package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private EditText nameEdit;
    private EditText descriptionEdit;
    private EditText dateEdit;
    private Spinner spinner;

    SimpleDateFormat dateFormatter;

    String taskId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        final SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        flatId = prefs.getString("flatId", "");

        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        nameEdit = findViewById(R.id.name_editText);
        descriptionEdit = findViewById(R.id.description_editText);
        dateEdit = findViewById(R.id.date_editText);
        spinner = findViewById(R.id.periodicity_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.periodicity_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Intent intent = getIntent();
        if(intent != null){
            taskId = intent.getStringExtra("taskId");
            if(taskId != null) {
                fillFormWithTask(taskId);
            } else {
                spinner.setSelection(0);
            }
        }

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

        getFlatUsers();

        RecyclerView userList = findViewById(R.id.users_recyclerView);
        userList.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new Adapter();
        userList.setAdapter(usersAdapter);
    }

    private void getFlatUsers() {
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
                                usersAdapter.notifyItemInserted(flatUsers.size()-1);
                            }
                            onGetUsers();
                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void fillFormWithTask(String taskId){
        db.collection("Flats")
                .document(flatId)
                .collection("Tasks")
                .document(taskId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map taskData = document.getData();

                            String taskName = (String)taskData.get("Name");
                            String taskDescription = (String)taskData.get("Description");
                            Timestamp initialTimestamp = (Timestamp) taskData.get("Initial date");
                            Date initialDate = initialTimestamp.toDate();
                            String taskDate = dateFormatter.format(initialDate);
                            int periodicity = Math.round((Long)taskData.get("Periodicity"));

                            nameEdit.setText(taskName);
                            descriptionEdit.setText(taskDescription);
                            dateEdit.setText(taskDate);
                            spinner.setSelection(periodicity);

                            Log.d("test", "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d("test", "No such document");
                        }
                    } else {
                        Log.d("test", "get failed with ", task.getException());
                    }
                }
        });
    }

    private void saveTaskToFirestore() {
        if(taskId == null) {
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
        } else {
            db.collection("Flats")
                    .document(flatId)
                    .collection("Tasks")
                    .document(taskId)
                    .set(task)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("test", "DocumentSnapshot successfully written!");
                            setResult(RESULT_OK);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("test", "Error writing document", e);
                            setResult(RESULT_CANCELED);
                        }
                    });
        }
    }

    private void onGetUsers() {
        usersAdapter.notifyItemRangeInserted(0, flatUsers.size());
    }

    // Recyclerview of users
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView userView;
        Button upButton;
        Button downButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

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
        }

        @Override
        public int getItemCount() {
            return flatUsers.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(taskId != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.edit_task_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_task:
                db.collection("Flats")
                        .document(flatId)
                        .collection("Tasks")
                        .document(taskId)
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
