package ninja.sakib.kutupicker.viewholders;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ninja.sakib.kutupicker.R;

public class LoadedImageVH extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public CheckBox checkBox;

    public LoadedImageVH(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.loaded_image_item);
        checkBox = itemView.findViewById(R.id.loaded_image_item_checkbox);
    }
}
