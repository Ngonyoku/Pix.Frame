package com.example.pixframe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixframe.R;
import com.example.pixframe.model.Photos;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PhotosRVAdapter extends RecyclerView.Adapter<PhotosRVAdapter.PhotosViewHolder> {
    private Context context;
    private List<Photos> photosList;


    public PhotosRVAdapter(Context context, List<Photos> list) {
        this.context = context;
        this.photosList = list;
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photos, parent, false);
        return new PhotosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotosViewHolder holder, int position) {
        Photos photos = photosList.get(position);
        Picasso.get().
                load(photos.getImageUrl())
                .placeholder(R.color.colorPrimaryLight)
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
