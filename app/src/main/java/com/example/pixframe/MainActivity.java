package com.example.pixframe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pixframe.adapters.PhotosRVAdapter;
import com.example.pixframe.dialogs.LoadingDialog;
import com.example.pixframe.model.Photos;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int PICK_IMAGE_REQUEST = 1;
    public static String FIREBASE_PHOTOS_REF = "Photos";
    private boolean isShown;

    private Dialog uploadDialog;
    private LoadingDialog loadingDialog;
    private TextView choose_tv, capture_tv;
    private ImageView mPhotoSelected;
    private FloatingActionButton choose_fab, capture_fab;
    private Toolbar toolbar;
    private ProgressBar loadingProgress;

    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private PhotosRVAdapter rvAdapter;
    private List<Photos> photosList;
    private Uri mImageUri;
    //Firebase
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private StorageTask storageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choose_tv = findViewById(R.id.tv_choose);
        capture_tv = findViewById(R.id.tv_capture);
        choose_fab = findViewById(R.id.fab_choose);
        capture_fab = findViewById(R.id.fab_capture);
        toolbar = findViewById(R.id.toolbar);
        loadingProgress = findViewById(R.id.loading_prgrss);

        FirebaseUtil.openFirebaseReference(FIREBASE_PHOTOS_REF);
        storageRef = FirebaseUtil.mStorageReference;
        databaseRef = FirebaseUtil.mDatabaseReference;

        photosList = new ArrayList<>();

        loadingDialog = new LoadingDialog(this);
        uploadDialog = new Dialog(MainActivity.this);

        setSupportActionBar(toolbar);
        uploadDialog.setContentView(R.layout.dialog_upload_photo);
        uploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        // This method enables our App to receive updates in Realtime
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /*
                 * onDataChange() reads static snapshots of contents in the specified path in the
                 * db. The method is triggered when data changes in the specified DatabaseReference
                 * (databaseRef) in the db.
                 *
                 */
                photosList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //We Loop Through the data.
                    Photos upload = postSnapshot.getValue(Photos.class);
                    photosList.add(upload);
                }

                rvAdapter.notifyDataSetChanged();
                loadingProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                /*
                 * onCancelled() is Called when if the read is Cancelled e.g if the User doesn't have
                 * permission to access the data.
                 *
                 */
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingProgress.setVisibility(View.INVISIBLE);
            }
        });
        isShown = true;

        choose_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPhotos();
    }

    private void loadPhotos() {
        recyclerView = findViewById(R.id.recyclerView);
        rvAdapter = new PhotosRVAdapter(MainActivity.this, photosList);
        recyclerView.setAdapter(rvAdapter);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
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

    void openDialog() {
        final TextInputEditText caption_ted = uploadDialog.findViewById(R.id.ted_caption);
        mPhotoSelected = uploadDialog.findViewById(R.id.photo_selected);

        uploadDialog.findViewById(R.id.btn_upload).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (storageTask != null && storageTask.isInProgress()) {
                            Toast.makeText(MainActivity.this, R.string.upload_message, Toast.LENGTH_SHORT).show();
                        } else {
                            uploadPhoto(caption_ted.getText().toString());
                        }
                    }
                });

        uploadDialog.findViewById(R.id.btn_selectAnother)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chooseImage();
                    }
                });

        uploadDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public void chooseImage() {
        Intent pick = new Intent();
        startActivityForResult(pick
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT),
                PICK_IMAGE_REQUEST
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            mImageUri = data.getData();
            openDialog();
            Picasso.get().load(mImageUri).into(mPhotoSelected);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

    public void uploadPhoto(final String caption) {
        if (mImageUri != null) {

            loadingDialog.startLoading();
            StorageReference fileRef = storageRef.child(System.currentTimeMillis()
                    + "."
                    + getFileExtension(mImageUri));

            //Upload the image File to Firebase
            storageTask = fileRef.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(MainActivity.this, R.string.upload_successful, Toast.LENGTH_SHORT).show();
                            loadingDialog.dismissDialog();

                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful()) ;
                            Uri downloadUrl = urlTask.getResult();

                            Photos photo = new Photos(caption, downloadUrl.toString());

                            //Set Database Metadata For The Image.
                            String uploadId = databaseRef.push().getKey();
                            databaseRef.child(uploadId).setValue(photo);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            loadingDialog.dismissDialog();
                        }
                    });
        } else {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }
}
