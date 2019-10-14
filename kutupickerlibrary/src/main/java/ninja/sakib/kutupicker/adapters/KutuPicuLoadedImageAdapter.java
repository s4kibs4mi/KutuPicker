package ninja.sakib.kutupicker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ninja.sakib.kutupicker.R;
import ninja.sakib.kutupicker.listeners.OnItemSelectedListener;
import ninja.sakib.kutupicker.models.LoadedImage;
import ninja.sakib.kutupicker.viewholders.LoadedImageVH;

public class KutuPicuLoadedImageAdapter extends RecyclerView.Adapter<LoadedImageVH> {
    private Context ctx;
    private ArrayList<LoadedImage> loadedImages;
    private OnItemSelectedListener itemSelectedListener;

    public KutuPicuLoadedImageAdapter(Context ctx, ArrayList<LoadedImage> loadedImages, OnItemSelectedListener itemSelectedListener) {
        this.ctx = ctx;
        this.loadedImages = loadedImages;
        this.itemSelectedListener = itemSelectedListener;
    }

    @NonNull
    @Override
    public LoadedImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.loaded_image_item, parent, false);
        return new LoadedImageVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LoadedImageVH holder, final int position) {
        Log.d("Position Bind", position + "");

        holder.setIsRecyclable(false);

        LoadedImage loadedImage = loadedImages.get(position);

        Glide.with(ctx).load(loadedImage.getImage()).into(holder.imageView);
        holder.checkBox.setChecked(loadedImage.isChecked());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("Position", position + "");

                loadedImages.get(position).setChecked(isChecked);
                itemSelectedListener.onSelected(loadedImages.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.loadedImages.size();
    }
}
