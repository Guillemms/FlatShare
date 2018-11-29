package com.example.guillemms.flatshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

public class FlatActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView ID;
    private EditText name;
    private Button create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flat);

        ID = findViewById(R.id.text_view_id);
        name = findViewById(R.id.editText);
        create = findViewById(R.id.button_create);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity();
            }
        });
    }

    private void activity() {
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
    }
}
