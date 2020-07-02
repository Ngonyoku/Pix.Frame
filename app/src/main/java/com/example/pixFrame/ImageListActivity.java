package com.example.pixFrame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pixFrame.adapters.ImageRecyclerViewAdapter;
import com.example.pixFrame.dialogs.DialogBuilder;
import com.example.pixFrame.model.Photos;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.example.pixFrame.dialogs.DialogBuilder.*;

public class ImageListActivity extends AppCompatActivity {
    public static final int REQUEST_SELECT_IMAGE = 1;
    public static final String IMAGE_FILE_AUTHORITY = "com.example.pixFrame.fileprovider";
    public static String FIREBASE_PHOTOS_REF = "Photos";
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_TAKE_PHOTO = 3;

    String currentPhotoPath;

    private Dialog uploadDialog;
    private ImageView mPhotoSelected;
    private ProgressBar mProgressBar;

    private ImageRecyclerViewAdapter rvAdapter;
    private Uri mImageUri;

    //Firebase
    private DatabaseReference databaseRef;
    private StorageTask storageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        mProgressBar = findViewById(R.id.loading_prgrss);

        FirebaseUtil.openFirebaseReference(FIREBASE_PHOTOS_REF, ImageListActivity.this);
        databaseRef = FirebaseUtil.mDatabaseReference;
        uploadDialog = new Dialog(ImageListActivity.this);

        setSupportActionBar(toolbar);
        uploadDialog.setContentView(R.layout.dialog_upload_photo);
        Objects.requireNonNull(uploadDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    public ValueEventListener eventListener(final List<Photos> photosList) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /*
                 * onDataChange() reads static snapshots of contents in the specified path in the
                 * db. The method is triggered when data changes in the specified DatabaseReference
                 * (databaseRef) in the db.
                 *
                 */
                photosList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //We Loop Through the data.
                    Photos upload = postSnapshot.getValue(Photos.class);
                    photosList.add(upload);
                }

                rvAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                /*
                 * onCancelled() is Called when if the read is Cancelled e.g if the User doesn't have
                 * permission to access the data.
                 *
                 */
                Toast.makeText(ImageListActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.attachListener();
        loadPhotos();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    private void loadPhotos() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        rvAdapter = new ImageRecyclerViewAdapter(this);
        recyclerView.setAdapter(rvAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
    }

    public void chooseImage(View view) {
        DialogBuilder.createDialog(new AlertDialog.Builder(this)
                .setPositiveButton("Select Photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openFileChooser();
                    }
                })
                .setNegativeButton("Take Photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        takePicture();
                    }
                })
                .setTitle("Upload Image")
        );
    }

    private void takePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    //Error occurred while creating the imageFile.
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ImageFileCreation", ex.getMessage());
                }

                if (photoFile != null) {
                    Uri photoUri = FileProvider.getUriForFile(
                            this,
                            IMAGE_FILE_AUTHORITY, /*Authority*/
                            photoFile
                    );

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                }
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                createDialog(
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.permission_needed_title)
                                .setMessage("Please grant us permission to Access the camera so you can have a smooth interaction with the App")
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(
                                                ImageListActivity.this,
                                                new String[]{Manifest.permission.CAMERA},
                                                REQUEST_IMAGE_CAPTURE
                                        );
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                );
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            } else {
                takePicture();
            }
        }
    }

    //    private void addImageToGallery() {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(currentPhotoPath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            mImageUri = data.getData();
            openDialog();
            Picasso.get().load(mImageUri).into(mPhotoSelected);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode != RESULT_CANCELED) {
            mImageUri = FileProvider.getUriForFile(
                    this,
                    IMAGE_FILE_AUTHORITY,
                    new File(currentPhotoPath)
            );
            openDialog();
            Picasso.get().load(mImageUri).into(mPhotoSelected);
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /*Prefix*/
                ".jpg",       /*suffix*/
                storageDirectory    /*directory*/
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    void openDialog() {
        final TextInputEditText caption_ted = uploadDialog.findViewById(R.id.ted_caption);
        mPhotoSelected = uploadDialog.findViewById(R.id.photo_selected);

        uploadDialog.findViewById(R.id.btn_upload).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (storageTask != null && storageTask.isInProgress()) {
                            Toast.makeText(ImageListActivity.this, R.string.upload_message, Toast.LENGTH_SHORT).show();
                        } else {
                            uploadPhoto(Objects.requireNonNull(caption_ted.getText()).toString());
                        }
                    }
                });

        uploadDialog.findViewById(R.id.btn_selectAnother)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFileChooser();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                signOut();
                return true;
            case R.id.menu_exit:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ImageListActivity.this, "You are Logged Out", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openFileChooser() {
        Intent pick = new Intent();
        startActivityForResult(pick
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT),
                REQUEST_SELECT_IMAGE
        );
    }

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

    public void uploadPhoto(final String caption) {
        if (mImageUri != null) {

            FirebaseUtil.getStorageTask(mImageUri, getFileExtension(mImageUri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful()) ;
                            Uri downloadUrl = urlTask.getResult();

                            Photos photo = new Photos(caption, downloadUrl.toString());

                            //Set Database Metadata For The Image.
                            String uploadId = databaseRef.push().getKey();
                            assert uploadId != null;
                            databaseRef.child(uploadId).setValue(photo);
                            Toast.makeText(ImageListActivity.this, R.string.upload_successful, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ImageListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } else {
            Toast.makeText(this, R.string.no_file_selected, Toast.LENGTH_SHORT).show();
        }
    }
}