package com.example.pixframe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private boolean isShown;
    private TextView choose_tv, capture_tv;
    private FloatingActionButton choose_fab, capture_fab;
    private Uri imageUri;
    public static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choose_tv = findViewById(R.id.tv_choose);
        capture_tv = findViewById(R.id.tv_capture);
        choose_fab = findViewById(R.id.fab_choose);
        capture_fab = findViewById(R.id.fab_capture);
        isShown = true;

        choose_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }

    public void showFabs(View view) {
        if (isShown) {
            choose_tv.setVisibility(View.VISIBLE);
            capture_tv.setVisibility(View.VISIBLE);
            choose_fab.setVisibility(View.VISIBLE);
            capture_fab.setVisibility(View.VISIBLE);

            isShown = false;
        } else {
            choose_tv.setVisibility(View.INVISIBLE);
            capture_tv.setVisibility(View.INVISIBLE);
            choose_fab.setVisibility(View.INVISIBLE);
            capture_fab.setVisibility(View.INVISIBLE);

            isShown = true;
        }
    }

    private void chooseImage() {
        Intent pick = new Intent();
//        pick.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(pick.setType("image/*").setAction(Intent.ACTION_GET_CONTENT), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            imageUri = data.getData();
        }
    }
}
