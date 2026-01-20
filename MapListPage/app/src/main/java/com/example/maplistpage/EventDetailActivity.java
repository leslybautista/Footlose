package com.example.maplistpage;


import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        int eventId = getIntent().getIntExtra("event_id", -1);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        // TODO: Load event details using dbHelper.getEventById(eventId)

        // Back button
        Button backButton = findViewById(R.id.btn_back_to_list);
        backButton.setOnClickListener(v -> {
            // Finish this activity to go back to the previous fragment
            finish();
        });
    }
}