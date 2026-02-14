package com.daille.evolutioncards;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton playButton = findViewById(R.id.buttonPlay);
        MaterialButton shopButton = findViewById(R.id.buttonShop);
        MaterialButton settingsButton = findViewById(R.id.buttonSettings);
        MaterialButton collectionButton = findViewById(R.id.buttonCollection);
        MaterialButton rankingButton = findViewById(R.id.buttonRanking);

        playButton.setOnClickListener(v -> startActivity(new Intent(this, PlayActivity.class)));
        collectionButton.setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));

        shopButton.setOnClickListener(v -> openPlaceholder(getString(R.string.menu_shop)));
        settingsButton.setOnClickListener(v -> openPlaceholder(getString(R.string.menu_settings)));
        rankingButton.setOnClickListener(v -> openPlaceholder(getString(R.string.menu_ranking)));
    }

    private void openPlaceholder(String sectionTitle) {
        Intent intent = new Intent(this, PlaceholderActivity.class);
        intent.putExtra(PlaceholderActivity.EXTRA_TITLE, sectionTitle);
        startActivity(intent);
    }
}
