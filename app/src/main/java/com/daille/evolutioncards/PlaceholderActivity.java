package com.daille.evolutioncards;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlaceholderActivity extends AppCompatActivity {
    public static final String EXTRA_TITLE = "extra_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        TextView titleView = findViewById(R.id.placeholderTitle);
        titleView.setText(title == null ? getString(R.string.app_name) : title);
    }
}
