package com.example.pixFrame.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixFrame.FirebaseUtil;
import com.example.pixFrame.ImageListActivity;
import com.example.pixFrame.R;
import com.example.pixFrame.model.Photos;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.PhotosViewHolder> {
    private List<Photos> photosList;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    public ImageRecyclerViewAdapter(ImageListActivity imageListActivity) {
        FirebaseUtil.openFirebaseReference(ImageListActivity.FIREBASE_PHOTOS_REF, new ImageListActivity());
        storageRef = FirebaseUtil.mStorageReference;
        databaseRef = FirebaseUtil.mDatabaseReference;
        this.photosList = FirebaseUtil.mPhotosList;

        // This method enables our App to receive updates in Realtime
        databaseRef.addValueEventListener(imageListActivity.eventListener(photosList));
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photos, parent, false);
        return new PhotosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotosViewHolder holder, int position) {
        Photos photos = photosList.get(position);
        Picasso.get()
                .load(photos.getImageUrl())
                .into(holder.photo);
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }

    public class PhotosViewHolder extends RecyclerView.ViewHolder {
        public ImageView photo;
        TextView Caption;

        public PhotosViewHolder(@NonNull View itemView) {
            super(itemView);

            photo = itemView.findViewById(R.id.photos_img);
        }
    }
}
