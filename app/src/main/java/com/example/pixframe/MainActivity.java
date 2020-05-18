package com.example.pixframe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pixframe.model.Photos;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    public static final int PICK_IMAGE_REQUEST = 1;

    private boolean isShown;
    private TextView choose_tv, capture_tv;
    private FloatingActionButton choose_fab, capture_fab;
    private Uri imageUri;

    //Firebase
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choose_tv = findViewById(R.id.tv_choose);
        capture_tv = findViewById(R.id.tv_capture);
        choose_fab = findViewById(R.id.fab_choose);
        capture_fab = findViewById(R.id.fab_capture);
        isShown = true;
        storageRef = FirebaseStorage.getInstance().getReference("Photos");
        databaseRef = FirebaseDatabase.getInstance().getReference("Photos");

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

    public void chooseImage() {
        Intent pick = new Intent();
        startActivityForResult(
                pick.setType("image/*").setAction(Intent.ACTION_GET_CONTENT),
                PICK_IMAGE_REQUEST);
        uploadPhoto();
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

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

    public void uploadPhoto() {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis()
                    + "."
                    + getFileExtension(imageUri));

            //Upload the image File to Firebase
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(MainActivity.this, "Upload was SuccessFul", Toast.LENGTH_SHORT).show();
                            Photos photo = new Photos("", taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());

                            //Set Database Metadata For The Image.
                            String uploadId = databaseRef.push().getKey();
                            databaseRef.child(uploadId).setValue(photo);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                }
            });
        } else {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

}
