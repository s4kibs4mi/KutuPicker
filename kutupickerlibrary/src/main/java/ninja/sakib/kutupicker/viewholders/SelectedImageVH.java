package ninja.sakib.kutupicker.viewholders;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ninja.sakib.kutupicker.R;

public class SelectedImageVH extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public ImageView removeImage;

    public SelectedImageVH(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.selected_image_item);
        removeImage = itemView.findViewById(R.id.selected_image_item_remove);
    }
}
