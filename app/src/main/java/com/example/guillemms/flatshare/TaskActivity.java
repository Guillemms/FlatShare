package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {

    Map<String, Object> task = new HashMap<String, Object>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    String flatId;

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

        /*spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                task.put("Periodicity", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                task.put("Periodicity", 0);
            }
        });*/

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
                            ArrayList<String> userIds = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userIds.add(document.getId());
                            }
                            onUserIds(userIds);
                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }
                    }
                });
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

    private void onUserIds(ArrayList<String> userIds) {
        task.put("User IDs", userIds);
    }

}
