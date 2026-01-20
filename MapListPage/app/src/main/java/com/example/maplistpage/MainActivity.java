package com.example.maplistpage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.btm_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.rel_layout, new MapFragment()).commit();

    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        if (item.getItemId() == R.id.map)
                fragment = new MapFragment();

        if (item.getItemId() ==  R.id.events)
                fragment = new EventsListFragment();

        if (item.getItemId() ==  R.id.tutorials)
                fragment = new TutorialsFragment();

        if (fragment != null)
            getSupportFragmentManager().beginTransaction().replace(R.id.rel_layout, fragment).commit();

        return true;
    }
}