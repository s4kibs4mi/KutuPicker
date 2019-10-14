package ninja.sakib.kutupicker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ninja.sakib.kutupicker.R;
import ninja.sakib.kutupicker.listeners.OnItemRemovedListener;
import ninja.sakib.kutupicker.models.SelectedImage;
import ninja.sakib.kutupicker.viewholders.SelectedImageVH;

public class KutuPicuSelectedImageAdapter extends RecyclerView.Adapter<SelectedImageVH> {
    private Context ctx;
    private ArrayList<SelectedImage> selectedImages;
    private OnItemRemovedListener itemRemovedListener;

    public KutuPicuSelectedImageAdapter(Context ctx, ArrayList<SelectedImage> selectedImages, OnItemRemovedListener itemRemovedListener) {
        this.ctx = ctx;
        this.selectedImages = selectedImages;
        this.itemRemovedListener = itemRemovedListener;
    }

    @NonNull
    @Override
    public SelectedImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.selected_image_item, parent, false);
        return new SelectedImageVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedImageVH holder, int position) {
        final SelectedImage selectedImage = selectedImages.get(position);

        Glide.with(ctx).load(selectedImage.getImage()).into(holder.imageView);
        holder.removeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemRemovedListener.onRemoved(selectedImage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedImages.size();
    }
}
