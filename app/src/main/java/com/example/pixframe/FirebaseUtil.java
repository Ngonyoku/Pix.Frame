package com.example.pixframe;

import com.example.pixframe.model.Photos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUtil {
    //Firebase
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;

    public static FirebaseUtil mFirebaseUtil;
    public static List<Photos> mPhotos;

    private FirebaseUtil() {
    }

    public static void openFirebaseReference(String reference) {
        if (mFirebaseUtil == null) {
            mFirebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();

            connectToStorage(reference);
        }
        mPhotos = new ArrayList<>();
        mDatabaseReference = mFirebaseDatabase.getReference(reference);
    }

    private static void connectToStorage(String reference) {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference(reference);
    }
}
